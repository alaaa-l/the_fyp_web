package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.Transaction;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DashboardController(ProjectRepository projectRepository, UserRepository userRepository,
            TransactionRepository transactionRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty())
            return "redirect:/login";

        User user = optionalUser.get();
        List<Project> myProjects = projectRepository.findByOwner(user);

        // Individual Stats
        int myProjectsCount = myProjects.size();
        double myTotalRaised = myProjects.stream().mapToDouble(Project::getRaisedAmount).sum();

        // Site General Stats
        long totalProjects = projectRepository.count();
        long totalUsers = userRepository.count();
        double totalRaised = projectRepository.findAll().stream().mapToDouble(Project::getRaisedAmount).sum();

        // --- Chart Data Calculations ---

        // 1. Project Distribution (Pie) - by Type
        Map<String, Long> projectTypeDist = myProjects.stream()
                .collect(Collectors.groupingBy(p -> p.getType().name(), Collectors.counting()));

        // 2. Funding Progress (Doughnut)
        long low = 0, medium = 0, high = 0, completed = 0;
        for (Project p : myProjects) {
            double pct = p.getFundingPercent();
            if (pct >= 100)
                completed++;
            else if (pct >= 75)
                high++;
            else if (pct >= 25)
                medium++;
            else
                low++;
        }
        Map<String, Long> fundingProgressDist = new LinkedHashMap<>();
        fundingProgressDist.put("Completed", completed);
        fundingProgressDist.put("High (75%+)", high);
        fundingProgressDist.put("Medium (25%-75%)", medium);
        fundingProgressDist.put("Low (<25%)", low);

        // 3. Recent Activity (Bar) - Transactions over last 6 months for MY projects
        List<Transaction> myTransactions = transactionRepository.findByProjectOwner(user);
        Map<String, Double> monthlyActivity = new LinkedHashMap<>();

        // Initialize last 6 months
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            Calendar m = (Calendar) cal.clone();
            m.add(Calendar.MONTH, -i);
            String monthName = m.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
            monthlyActivity.put(monthName, 0.0);
        }

        for (Transaction t : myTransactions) {
            if (t.getTimestamp() != null) {
                String mName = t.getTimestamp().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                if (monthlyActivity.containsKey(mName)) {
                    monthlyActivity.put(mName, monthlyActivity.get(mName) + t.getAmount());
                }
            }
        }

        model.addAttribute("myProjects", myProjects);
        model.addAttribute("recentTransactions", myTransactions.stream().limit(10).collect(Collectors.toList()));

        model.addAttribute("myProjectsCount", myProjectsCount);
        model.addAttribute("myTotalRaised", myTotalRaised);
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRaised", totalRaised);

        // Chart attributes
        model.addAttribute("typeDistKeys", projectTypeDist.keySet());
        model.addAttribute("typeDistValues", projectTypeDist.values());
        model.addAttribute("fundingKeys", fundingProgressDist.keySet());
        model.addAttribute("fundingValues", fundingProgressDist.values());
        model.addAttribute("activityKeys", monthlyActivity.keySet());
        model.addAttribute("activityValues", monthlyActivity.values());

        return "dashboard";
    }
}