param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$ConcurrentUsers = 20,
    [int]$RequestsPerUser = 5,
    [int]$RestaurantId = 1,
    [int]$MenuItemId = 1
)

$ErrorActionPreference = "Stop"

$resultsDir = Join-Path $PSScriptRoot "..\results"
if (-not (Test-Path $resultsDir)) {
    New-Item -ItemType Directory -Path $resultsDir | Out-Null
}

$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$outputPath = Join-Path $resultsDir "epic-8.3-smoke-$runId.json"

$username = "perf_user_$runId"
$password = "Password123!"
$phone = "+233$((Get-Random -Minimum 100000000 -Maximum 999999999))"

$registerBody = @{
    username = $username
    email = "$username@example.com"
    password = $password
    firstName = "Perf"
    lastName = "User"
    phone = $phone
    deliveryAddress = "18 Order Street"
    city = "Accra"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/register" -ContentType "application/json" -Body $registerBody -UseBasicParsing | Out-Null
} catch {
    # Continue; login will confirm whether credentials are usable.
}

$loginBody = @{ username = $username; password = $password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/login" -ContentType "application/json" -Body $loginBody -UseBasicParsing
$token = $loginResponse.token
if (-not $token) {
    throw "Could not acquire token for performance user."
}

$orderBody = @{
    restaurantId = $RestaurantId
    items = @(
        @{
            menuItemId = $MenuItemId
            quantity = 1
            specialInstructions = "Perf run"
        }
    )
    deliveryAddress = "18 Order Street"
    specialInstructions = "Perf run"
} | ConvertTo-Json -Depth 5

$jobs = @()
for ($u = 1; $u -le $ConcurrentUsers; $u++) {
    $jobs += Start-Job -ArgumentList $BaseUrl, $token, $RequestsPerUser, $orderBody -ScriptBlock {
        param($BaseUrlArg, $TokenArg, $CountArg, $OrderBodyArg)
        $local = @{ success = 0; failure = 0; statusCodes = @{} }
        for ($i = 1; $i -le $CountArg; $i++) {
            $code = "000"
            try {
                $response = Invoke-WebRequest -Method Post -Uri "$BaseUrlArg/api/orders" -Headers @{ Authorization = "Bearer $TokenArg" } -ContentType "application/json" -Body $OrderBodyArg -UseBasicParsing -ErrorAction Stop
                $code = [string]$response.StatusCode
            } catch {
                if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
                    $code = [string][int]$_.Exception.Response.StatusCode
                }
            }
            if ($code -eq "201") {
                $local.success++
            } else {
                $local.failure++
            }
            if (-not $local.statusCodes.ContainsKey($code)) {
                $local.statusCodes[$code] = 0
            }
            $local.statusCodes[$code]++
        }
        return $local
    }
}

Wait-Job -Job $jobs | Out-Null
$jobResults = Receive-Job -Job $jobs
$jobs | Remove-Job

$aggregateCodes = @{}
$totalSuccess = 0
$totalFailure = 0
foreach ($r in $jobResults) {
    $totalSuccess += [int]$r.success
    $totalFailure += [int]$r.failure
    foreach ($k in $r.statusCodes.Keys) {
        if (-not $aggregateCodes.ContainsKey($k)) {
            $aggregateCodes[$k] = 0
        }
        $aggregateCodes[$k] += [int]$r.statusCodes[$k]
    }
}

$circuitBreakers = curl.exe -s "http://localhost:8083/actuator/circuitbreakers"
$circuitEvents = curl.exe -s "http://localhost:8083/actuator/circuitbreakerevents"
$rabbitOverview = curl.exe -s -u guest:guest "http://localhost:15672/api/overview"
$dbPoolActive = curl.exe -s "http://localhost:8083/actuator/metrics/hikaricp.connections.active"
$dbPoolPending = curl.exe -s "http://localhost:8083/actuator/metrics/hikaricp.connections.pending"
$queueDepth = curl.exe -s -u guest:guest "http://localhost:15672/api/queues/%2F/delivery.order-placed"

$result = [ordered]@{
    runId = $runId
    generatedUser = $username
    baseUrl = $BaseUrl
    concurrentUsers = $ConcurrentUsers
    requestsPerUser = $RequestsPerUser
    totalRequests = ($ConcurrentUsers * $RequestsPerUser)
    successfulOrders = $totalSuccess
    failedOrders = $totalFailure
    statusCodeBreakdown = $aggregateCodes
    actuator = [ordered]@{
        circuitBreakers = ($circuitBreakers | ConvertFrom-Json)
        circuitBreakerEvents = ($circuitEvents | ConvertFrom-Json)
        dbPoolActive = ($dbPoolActive | ConvertFrom-Json)
        dbPoolPending = ($dbPoolPending | ConvertFrom-Json)
    }
    rabbitmq = [ordered]@{
        overview = ($rabbitOverview | ConvertFrom-Json)
        orderPlacedQueue = ($queueDepth | ConvertFrom-Json)
    }
}

$result | ConvertTo-Json -Depth 8 | Set-Content -Path $outputPath
Write-Host "Performance smoke completed. Output: $outputPath"
