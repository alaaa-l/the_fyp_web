package com.capstone.OpportuGrow.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Double amount;
    private String type; // DONATION, LOAN, etc.
    private LocalDateTime timestamp;
    private String status; // If applicable, or just "COMPLETED"
}
