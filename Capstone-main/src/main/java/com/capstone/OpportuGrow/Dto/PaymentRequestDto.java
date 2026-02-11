package com.capstone.OpportuGrow.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private Long projectId;
    private Double amount;
    private String currency; // Default to "usd"
}
