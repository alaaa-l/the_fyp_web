package com.capstone.OpportuGrow.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultantCreateDto {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String specialty;
}
