package com.capstone.OpportuGrow.Service;

import com.capstone.OpportuGrow.Repository.NotificationRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.model.Notification;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository, NotificationRepository notificationRepository,
            NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    public Project createProject(com.capstone.OpportuGrow.Dto.ProjectRegisterDto dto,
            com.capstone.OpportuGrow.model.User owner) {
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setShortDescription(dto.getShortDescription());
        project.setLongDescription(dto.getLongDescription());
        project.setCategory(dto.getCategory());
        project.setFundingGoal(dto.getFundingGoal());
        project.setFundingDuration(dto.getFundingDuration());
        project.setImageUrl(dto.getImageUrl());
        project.setOwner(owner);
        project.setStatus(ProjectStatus.PENDING);
        project.setType(dto.getType());
        project.setPhoneNumber(dto.getPhoneNumber());
        project.setAddress(dto.getAddress());
        project.setCreatedAt(new java.util.Date());

        // Fee Calculation
        java.math.BigDecimal goal = java.math.BigDecimal.valueOf(dto.getFundingGoal());
        java.math.BigDecimal platformFee = goal.multiply(new java.math.BigDecimal("0.05"));
        java.math.BigDecimal processingFee = goal.multiply(new java.math.BigDecimal("0.029"));
        java.math.BigDecimal receive = goal.subtract(platformFee).subtract(processingFee);

        project.setPlatformFee(platformFee);
        project.setProcessingFee(processingFee);
        project.setAmountYouWillReceive(receive);

        return projectRepository.save(project);
    }

    public void approveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setStatus(ProjectStatus.APPROVED);
        projectRepository.save(project);

        // Send notification to user
        Notification notification = new Notification();
        notification.setUser(project.getOwner());
        notification.setMessage("Your project '" + project.getTitle() + "' has been approved!");
        notification.setLink("/projects/" + project.getId()); // رابط للصفحة المنشورة
        notificationRepository.save(notification);
    }

    public void rejectProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setStatus(ProjectStatus.REJECTED);
        projectRepository.save(project);

        // Send notification to user
        Notification notification = new Notification();
        notification.setUser(project.getOwner());
        notification.setMessage("Your project '" + project.getTitle() + "' was rejected. Contact admin for details.");
        notification.setLink("/chat?userId=" + project.getOwner().getId()); // رابط للدردشة
        notificationRepository.save(notification);
    }

    public List<Project> getPendingProjects() {
        return projectRepository.findByStatus(ProjectStatus.PENDING);
    }

}
