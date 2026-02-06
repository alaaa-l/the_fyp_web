package com.capstone.OpportuGrow.Dto;

import com.capstone.OpportuGrow.model.Gender;
import com.capstone.OpportuGrow.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private boolean active;
    private String ProfileImage;
    private String occupation;
    private Gender gender;
    private Date dob;
}
