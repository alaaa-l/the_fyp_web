package com.capstone.OpportuGrow.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDto {

    private Long id;
    private Long consultantId;
    private String consultantName;
    private Integer userId;
    private String userName;
    private String date;
    private String time;
    private Integer duration;
    private String topic;
    private String status;
    private String notes;
}
