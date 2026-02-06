package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.AuthResponseDto;
import com.capstone.OpportuGrow.Dto.LoginDto;
import com.capstone.OpportuGrow.Dto.RegisterDto;
import com.capstone.OpportuGrow.Dto.UserResponseDto;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Role;
import com.capstone.OpportuGrow.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final com.capstone.OpportuGrow.Security.JwtUtil jwtUtil;

        public ApiAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        com.capstone.OpportuGrow.Security.JwtUtil jwtUtil) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
                this.jwtUtil = jwtUtil;
        }

        @PostMapping("/register")
        public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterDto registerDto) {
                if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
                        return ResponseEntity.badRequest().body(AuthResponseDto.builder()
                                        .success(false)
                                        .message("Passwords do not match")
                                        .build());
                }

                if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(AuthResponseDto.builder()
                                        .success(false)
                                        .message("Email already exists")
                                        .build());
                }

                User user = new User();
                user.setName(registerDto.getName());
                user.setEmail(registerDto.getEmail());
                user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
                user.setPhone(registerDto.getPhone());
                user.setAddress(registerDto.getAddress());
                user.setRole(Role.MEMBER);
                user.setCreation(new Date());
                user.setActive(true);

                User savedUser = userRepository.save(user);

                UserResponseDto userResponse = UserResponseDto.builder()
                                .id(savedUser.getId())
                                .name(savedUser.getName())
                                .email(savedUser.getEmail())
                                .phone(savedUser.getPhone())
                                .address(savedUser.getAddress())
                                .role(savedUser.getRole())
                                .active(savedUser.isActive())
                                .ProfileImage(savedUser.getProfileImage())
                                .occupation(savedUser.getOccupation())
                                .gender(savedUser.getGender())
                                .dob(savedUser.getDob())
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(AuthResponseDto.builder()
                                                .success(true)
                                                .message("User registered successfully")
                                                .user(userResponse)
                                                .token(jwtUtil.generateToken(savedUser.getEmail()))
                                                .build());
        }

        @PostMapping("/login")
        public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
                                                        loginDto.getPassword()));

                        if (authentication.isAuthenticated()) {
                                User user = userRepository.findByEmail(loginDto.getEmail())
                                                .orElseThrow(() -> new RuntimeException(
                                                                "User not found after successful authentication"));

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

                                String token = jwtUtil.generateToken(user.getEmail());

                                return ResponseEntity.ok(AuthResponseDto.builder()
                                                .success(true)
                                                .message("Login successful")
                                                .user(userResponse)
                                                .token(token)
                                                .build());
                        } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(AuthResponseDto.builder()
                                                                .success(false)
                                                                .message("Authentication failed")
                                                                .build());
                        }
                } catch (AuthenticationException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(AuthResponseDto.builder()
                                                        .success(false)
                                                        .message("Invalid email or password")
                                                        .build());
                }
        }
}
