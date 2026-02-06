package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.*;
import com.capstone.OpportuGrow.Service.ConsultantService;
import com.capstone.OpportuGrow.model.Consultant;
import com.capstone.OpportuGrow.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/consultants")
public class ApiAdminConsultantController {

    private final ConsultantService consultantService;

    public ApiAdminConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @PostMapping
    public ResponseEntity<ConsultantRegistrationResponseDto> createConsultant(
            @Valid @RequestBody ConsultantCreateDto dto) {
        try {
            Consultant consultant = consultantService.createConsultant(dto);
            User user = consultant.getUser();

            UserResponseDto userResponse = UserResponseDto.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .role(user.getRole())
                    .active(user.isActive())
                    .ProfileImage(user.getProfileImage())
                    .occupation(user.getOccupation())
                    .gender(user.getGender())
                    .dob(user.getDob())
                    .build();

            ConsultantDto consultantDto = ConsultantDto.builder()
                    .id(consultant.getId())
                    .name(consultant.getName())
                    .email(consultant.getEmail())
                    .specialty(consultant.getSpecialty())
                    .active(consultant.isActive())
                    .user(userResponse)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ConsultantRegistrationResponseDto.builder()
                            .success(true)
                            .message("Consultant created successfully")
                            .consultant(consultantDto)
                            .build());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ConsultantRegistrationResponseDto.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
}
