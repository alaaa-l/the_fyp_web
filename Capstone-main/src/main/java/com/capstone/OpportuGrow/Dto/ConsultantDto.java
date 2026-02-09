package com.capstone.OpportuGrow.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultantDto {
    private int id;
    private String name;
    private String email;
    private String specialty;
    private boolean active;
    private UserResponseDto user;
    private String profileImage;
}
