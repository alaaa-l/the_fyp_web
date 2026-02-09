package com.capstone.OpportuGrow.Service;

import com.capstone.OpportuGrow.Dto.ConsultantCreateDto;
import com.capstone.OpportuGrow.Repository.ConsultantRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.Dto.AvailabilityDto;
import com.capstone.OpportuGrow.Repository.AvailabilityRepository;
import com.capstone.OpportuGrow.model.Availability;
import com.capstone.OpportuGrow.model.Consultant;
import com.capstone.OpportuGrow.model.Role;
import com.capstone.OpportuGrow.model.User;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.capstone.OpportuGrow.Dto.ConsultantDto;
import com.capstone.OpportuGrow.Dto.UserResponseDto;
import java.util.Optional;

@Service
public class ConsultantService {

    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public ConsultantService(UserRepository userRepository,
            ConsultantRepository consultantRepository,
            AvailabilityRepository availabilityRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.consultantRepository = consultantRepository;
        this.availabilityRepository = availabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Consultant createConsultant(ConsultantCreateDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(Role.CONSULTANT);
        user.setActive(true);
        user.setCreation(new Date());

        User savedUser = userRepository.save(user);

        Consultant consultant = new Consultant();
        consultant.setUser(savedUser);
        consultant.setName(savedUser.getName());
        consultant.setEmail(savedUser.getEmail());
        consultant.setPassword(savedUser.getPassword());
        consultant.setSpecialty(dto.getSpecialty());
        consultant.setActive(true);

        return consultantRepository.save(consultant);
    }

    public List<ConsultantDto> getAllConsultants() {
        return consultantRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ConsultantDto> getConsultantById(Long id) {
        return consultantRepository.findById(id)
                .map(this::mapToDto);
    }

    public List<AvailabilityDto> getConsultantAvailability(Long consultantId) {
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new RuntimeException("Consultant not found"));

        return availabilityRepository.findByConsultant(consultant).stream()
                .map(a -> new AvailabilityDto(
                        a.getDay().toString(),
                        a.getStartTime(),
                        a.getEndTime()))
                .collect(Collectors.toList());
    }

    public ConsultantDto mapToDto(Consultant consultant) {
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

        return ConsultantDto.builder()
                .id(consultant.getId())
                .name(consultant.getName())
                .email(consultant.getEmail())
                .specialty(consultant.getSpecialty())
                .active(consultant.isActive())
                .user(userResponse)
                .profileImage(consultant.getProfileImage())
                .build();
    }
}
