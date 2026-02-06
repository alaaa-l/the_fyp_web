package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.ProjectRegisterDto;
import com.capstone.OpportuGrow.Service.ProjectService;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.User;
import com.capstone.OpportuGrow.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/projects")
public class ApiProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ApiProjectController(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody @Valid ProjectRegisterDto dto, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project createdProject = projectService.createProject(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }
}
