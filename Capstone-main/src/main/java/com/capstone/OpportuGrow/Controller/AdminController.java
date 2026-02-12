package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.AppointmentRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.model.ProjectType;
import com.capstone.OpportuGrow.model.Transaction;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")

public class AdminController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TransactionRepository transactionRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminController(UserRepository userRepository, ProjectRepository projectRepository,
            TransactionRepository transactionRepository, AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.transactionRepository = transactionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Project> projects = projectRepository.findAll();

        int totalProjects = projects.size();

        // New statistics as per requirements
        double totalRevenue = transactionRepository.findAll().stream().mapToDouble(Transaction::getAmount).sum();
        long totalUsers = userRepository.count();
        long pendingApproval = projects.stream().filter(p -> p.getStatus() == ProjectStatus.PENDING).count();
        long completedProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.COMPLETED).count();

        // Keep these for the charts
        long approvedProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.APPROVED).count();
        long rejectedProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.REJECTED).count();

        // Monthly stats arrays (Jan-Dec)
        int[] projectsPerMonth = new int[12];
        double[] fundsPerMonth = new double[12];
        int[] usersPerMonth = new int[12];

        // 1. Calculate Projects per Month
        projects.forEach(p -> {
            if (p.getCreatedAt() != null) {
                int month = p.getCreatedAt().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .getMonthValue() - 1;
                projectsPerMonth[month]++;
            }
        });

        // 2. Calculate Funds per Month (from Transactions)
        List<Transaction> transactions = transactionRepository.findAll();
        transactions.forEach(t -> {
            if (t.getTimestamp() != null) {
                int month = t.getTimestamp().getMonthValue() - 1;
                fundsPerMonth[month] += t.getAmount();
            }
        });

        // 3. Calculate Users per Month
        List<User> users = userRepository.findAll();
        users.forEach(u -> {
            if (u.getCreation() != null) {
                int month = u.getCreation().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .getMonthValue() - 1;
                usersPerMonth[month]++;
            }
        });

        // New statistics for the cards
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingApproval", pendingApproval);

        // Success rate based on completed projects
        double rawSuccessRate = totalProjects > 0 ? ((double) completedProjects / totalProjects * 100) : 0;
        model.addAttribute("successRate", Math.round(rawSuccessRate * 10.0) / 10.0);

        // Keep these for the charts
        model.addAttribute("approvedProjects", approvedProjects);
        model.addAttribute("rejectedProjects", rejectedProjects);
        model.addAttribute("pendingProjects", pendingApproval);
        model.addAttribute("completedProjects", completedProjects);

        // Project type distribution for pie chart
        long charityProjects = projects.stream().filter(p -> p.getType() == ProjectType.CHARITY).count();
        long fundProjects = projects.stream().filter(p -> p.getType() == ProjectType.FUND).count();
        long loanProjects = projects.stream().filter(p -> p.getType() == ProjectType.LOAN).count();

        model.addAttribute("charityProjects", charityProjects);
        model.addAttribute("fundProjects", fundProjects);
        model.addAttribute("loanProjects", loanProjects);

        // Engagement stats (Likes & Comments) by Type
        long[] likesByType = new long[3]; // Charity, Fund, Loan
        long[] commentsByType = new long[3];

        projects.forEach(p -> {
            int typeIndex = -1;
            if (p.getType() == ProjectType.CHARITY)
                typeIndex = 0;
            else if (p.getType() == ProjectType.FUND)
                typeIndex = 1;
            else if (p.getType() == ProjectType.LOAN)
                typeIndex = 2;

            if (typeIndex != -1) {
                likesByType[typeIndex] += (p.getLikedUsers() != null ? p.getLikedUsers().size() : 0);
                commentsByType[typeIndex] += (p.getComments() != null ? p.getComments().size() : 0);
            }
        });

        model.addAttribute("likesByType", likesByType);
        model.addAttribute("commentsByType", commentsByType);

        // Contribution amounts by Project Type
        double charityContributions = 0, fundContributions = 0, loanContributions = 0;
        for (Transaction t : transactions) {
            if (t.getProject() != null && t.getProject().getType() != null) {
                if (t.getProject().getType() == ProjectType.CHARITY)
                    charityContributions += t.getAmount();
                else if (t.getProject().getType() == ProjectType.FUND)
                    fundContributions += t.getAmount();
                else if (t.getProject().getType() == ProjectType.LOAN)
                    loanContributions += t.getAmount();
            }
        }
        model.addAttribute("charityContrib", charityContributions);
        model.addAttribute("fundContrib", fundContributions);
        model.addAttribute("loanContrib", loanContributions);

        // Appointment stats for Gauge Chart
        long approvedAppts = appointmentRepository
                .countByStatus(com.capstone.OpportuGrow.model.AppointmentStatus.APPROVED);
        long rejectedAppts = appointmentRepository
                .countByStatus(com.capstone.OpportuGrow.model.AppointmentStatus.REJECTED);
        model.addAttribute("approvedAppts", approvedAppts);
        model.addAttribute("rejectedAppts", rejectedAppts);

        // Chart data
        model.addAttribute("projectsPerMonth", projectsPerMonth);
        model.addAttribute("fundsPerMonth", fundsPerMonth);
        model.addAttribute("usersPerMonth", usersPerMonth);
        model.addAttribute("months",
                List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));

        model.addAttribute("lastUpdated",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("contentTemplate", "admin-dashboard");

        return "admin-layout"; // تأكد إنك عم ترجع الـ Layout الأساسي
    }; // بدون .html

}