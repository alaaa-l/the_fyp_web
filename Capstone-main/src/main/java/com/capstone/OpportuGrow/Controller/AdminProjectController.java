package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProjectController {
    private final ProjectRepository projectRepository;

    public AdminProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("/projects")
    public String adminProjects(Model model) {
        List<Project> projects = projectRepository.findAll();
        model.addAttribute("projects", projects);
        model.addAttribute("pageTitle", "Projects");
        model.addAttribute("contentTemplate", "admin-projects"); // بدون .html
        return "admin-layout";
    }

    @GetMapping("/projects/view/{id}")
    public String viewProject(@PathVariable("id") Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project Id: " + id));

        model.addAttribute("project", project);
        return "admin-projects-view"; // هذا اسم ملف الـ HTML الجديد
    }

    @PostMapping("/projects/delete/{id}")
    public String deleteProject(@PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            projectRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Project deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete project: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }

}
