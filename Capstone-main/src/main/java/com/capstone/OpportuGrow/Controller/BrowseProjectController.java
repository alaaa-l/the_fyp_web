package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.CommentRepository;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.Service.StripeService;
import com.capstone.OpportuGrow.model.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/projects")
public class BrowseProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final StripeService stripeService;

    public BrowseProjectController(CommentRepository commentRepository,
            ProjectRepository projectRepository, UserRepository userRepository,
            StripeService stripeService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.stripeService = stripeService;
    }

    @GetMapping
    public String browseProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProjectType type,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            Principal principal) {
        // Fetch projects according to filters
        List<Project> projects;
        List<ProjectStatus> visibleStatuses = List.of(ProjectStatus.APPROVED, ProjectStatus.COMPLETED);

        if ((keyword == null || keyword.isEmpty()) && type == null) {
            projects = projectRepository.findByStatusIn(visibleStatuses);
        } else if (keyword != null && !keyword.isEmpty() && type != null) {
            projects = projectRepository.searchVisibleByType(keyword, type);
        } else if (keyword != null && !keyword.isEmpty()) {
            projects = projectRepository.searchVisibleProjects(keyword);
        } else { // type != null
            projects = projectRepository.findByStatusInAndType(visibleStatuses, type);
        }

        // إذا عندك userRepository
        Optional<User> currentUser = principal != null ? userRepository.findByEmail(principal.getName()) : null;
        for (Project project : projects) {
            project.calculateFundingPercent(); // يحسب funding %
            if (currentUser != null) {
                project.setLikedByCurrentUser(project.getLikedUsers().contains(currentUser));
            } else {
                project.setLikedByCurrentUser(false);
            }
        }

        // Pagination
        int pageSize = 6; // projects per page
        int totalProjects = projects.size();
        int totalPages = (int) Math.ceil((double) totalProjects / pageSize);
        page = Math.max(1, Math.min(page, totalPages)); // clamp page number

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalProjects);

        List<Project> pageProjects = projects.subList(fromIndex, toIndex);
        Page<Project> projectPage = new PageImpl<>(pageProjects, PageRequest.of(page - 1, pageSize), totalProjects);

        // Add attributes
        model.addAttribute("projects", pageProjects);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "projects";
    }

    // Like project
    @PostMapping("/{id}/like")
    public String likeProject(@PathVariable Integer id, Principal principal) {
        if (principal == null)
            return "redirect:/login";

        Long projId = Long.valueOf(id);
        Project project = projectRepository.findById(projId).orElseThrow();
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Toggle Logic: إذا موجود بشيله، إذا مش موجود بزيذه
        if (project.getLikedUsers().contains(user)) {
            project.getLikedUsers().remove(user);
        } else {
            project.getLikedUsers().add(user);
        }

        projectRepository.save(project);
        return "redirect:/projects";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Integer id, @RequestParam String content, Principal principal) {
        if (principal == null)
            return "redirect:/login";

        Project project = projectRepository.findById(Long.valueOf(id)).orElseThrow();
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setProject(project);
        comment.setUser(user);
        comment.setCreatedAt(java.time.LocalDateTime.now());

        commentRepository.save(comment); // تأكد إنك عملت Autowired للـ commentRepository

        return "redirect:/projects/" + id; // بيرجعك لصفحة تفاصيل المشروع
    }

    @GetMapping("/{id}/agreement")
    public String showAgreementPage(
            @PathVariable Long id,
            @RequestParam("amount") Double amount, // Hon mnekhod l-mablagh mn l-modal
            Model model,
            HttpServletRequest request,
            Principal principal) {

        // 1. Njib l-Project mn l-DB
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project Id:" + id));

        // 2. Njib l-User li feteh halla2 (Lender/Investor)
        User currentUser = userRepository.findByEmail(principal.getName()).get();

        // 3. Nb3at kel l-data lal Agreement Page
        model.addAttribute("project", project);
        model.addAttribute("investor", currentUser);
        model.addAttribute("amount", amount); // L-mablagh li katabo bil-modal
        model.addAttribute("date", java.time.LocalDate.now());
        model.addAttribute("userIp", request.getRemoteAddr());

        return "agreement-page"; // Safhet l-3a2ed li 3melneha
    }

    @GetMapping("/{id}/payment")
    public String showPaymentPage(@PathVariable Long id, @RequestParam Double amount, Model model, Principal principal)
            throws StripeException {
        if (principal == null)
            return "redirect:/login";

        Project project = projectRepository.findById(id).get();
        User user = userRepository.findByEmail(principal.getName()).get();

        // Create PaymentIntent
        PaymentIntent intent = stripeService.createPaymentIntent(
                amount,
                "usd",
                project.getId().toString(),
                String.valueOf(user.getId()),
                project.getType().toString());

        model.addAttribute("project", project);
        model.addAttribute("amount", amount);
        model.addAttribute("clientSecret", intent.getClientSecret());
        model.addAttribute("stripePublicKey", stripeService.getPublicKey());

        return "payment-page";
    }

    @GetMapping("/{id}")
    public String showProjectDetails(@PathVariable int id, Model model, Principal principal) {
        Project project = projectRepository.findById((long) id).orElseThrow();

        // تأكد إنك حاسب النسبة كرمال الـ Progress Bar يبيّن
        project.calculateFundingPercent();

        // هيدي أهم جملة: لازم الاسم يكون "project" بالظبط
        model.addAttribute("project", project);

        return "project-details";
    }
}
