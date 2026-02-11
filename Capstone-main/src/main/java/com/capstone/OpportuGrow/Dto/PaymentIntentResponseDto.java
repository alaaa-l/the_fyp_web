package com.capstone.OpportuGrow.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentResponseDto {
    private String clientSecret;
    private String publishableKey;
    private String paymentIntentId;
    private Long projectId;
    private Double amount;
}
