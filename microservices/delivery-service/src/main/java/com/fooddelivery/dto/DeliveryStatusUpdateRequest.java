package com.fooddelivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryStatusUpdateRequest {
    @NotBlank
    private String status;
}
