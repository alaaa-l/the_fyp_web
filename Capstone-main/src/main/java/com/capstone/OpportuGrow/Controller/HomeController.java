package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.model.Project;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import com.capstone.OpportuGrow.model.ProjectStatus;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {
        private final ProjectRepository projectRepository;
        private final UserRepository userRepository;

        public HomeController(ProjectRepository projectRepository, UserRepository userRepository) {
                this.projectRepository = projectRepository;
                this.userRepository = userRepository;
        }

        @GetMapping("/")
        public String index(Model model) {
                // Fetch all projects for calculations
                List<Project> allProjects = projectRepository.findAll();

                // 1. Live Projects (Approved & Completed)
                List<Project> visibleProjects = allProjects.stream()
                                .filter(p -> p.getStatus() == ProjectStatus.APPROVED
                                                || p.getStatus() == ProjectStatus.COMPLETED)
                                .peek(Project::calculateFundingPercent) // Ensure percent is calculated
                                .collect(Collectors.toList());
                model.addAttribute("liveProjectsCount", visibleProjects.size());

                // 2. Total Raised (Sum of raisedAmount for all projects or just
                // approved/completed)
                // Usually finance sites show total historical raised
                double totalRaised = allProjects.stream()
                                .mapToDouble(p -> p.getRaisedAmount() != null ? p.getRaisedAmount() : 0.0)
                                .sum();
                model.addAttribute("totalRaised", totalRaised);

                // 3. Active Users
                long activeUsersCount = userRepository.count();
                model.addAttribute("activeUsersCount", activeUsersCount);

                // 4. Success Rate (Completed / Total Non-Pending)
                long completedCount = allProjects.stream()
                                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                                .count();
                long totalForSuccess = allProjects.size();
                double successRate = totalForSuccess > 0 ? ((double) completedCount / totalForSuccess) * 100 : 0;
                model.addAttribute("successRate", (int) successRate);

                // Featured Projects (Top 3 Approved/Completed)
                List<Project> featuredProjects = visibleProjects.stream()
                                .limit(3)
                                .collect(Collectors.toList());
                model.addAttribute("featuredProjects", featuredProjects);

                model.addAttribute("name", "OpportuGrow");
                return "index";
        }
}