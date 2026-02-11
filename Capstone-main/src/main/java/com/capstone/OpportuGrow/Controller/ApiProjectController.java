package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.ProjectRegisterDto;
import com.capstone.OpportuGrow.Service.ProjectService;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.User;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.Dto.ProjectResponseDto;
import java.util.List;
import java.util.stream.Collectors;
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
    private final ProjectRepository projectRepository;

    public ApiProjectController(ProjectService projectService, UserRepository userRepository,
            ProjectRepository projectRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        List<ProjectResponseDto> dtos = projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private ProjectResponseDto mapToDto(Project project) {
        project.calculateFundingPercent();
        return ProjectResponseDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .shortDescription(project.getShortDescription())
                // Use projectStory, fallback to longDescription if null (for old projects)
                .projectStory(
                        project.getProjectStory() != null ? project.getProjectStory() : project.getLongDescription())
                .category(project.getCategory())
                .fundingGoal(project.getFundingGoal())
                .fundingDuration(project.getFundingDuration())
                .status(project.getStatus())
                .type(project.getType())
                .imageUrl(project.getImageUrl())
                // Handle potentially null owner
                .ownerName(project.getOwner() != null ? project.getOwner().getName() : "Unknown")
                .createdAt(project.getCreatedAt())
                .fundingPercent(project.getFundingPercent())
                .raisedAmount(project.getRaisedAmount())
                .urgent(project.isUrgent())
                .build();
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody @Valid ProjectRegisterDto dto, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project createdProject = projectService.createProject(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }
}
