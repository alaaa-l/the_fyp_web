package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ProposalRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Proposal;
import com.capstone.OpportuGrow.model.ProposalStatus;
import com.capstone.OpportuGrow.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProposalController {

    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;

    public ProposalController(ProposalRepository proposalRepository, UserRepository userRepository) {
        this.proposalRepository = proposalRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/propose/idea")
    public String showProposalForm(Model model) {
        model.addAttribute("proposal", new Proposal());
        return "proposal-create";
    }

    @PostMapping("/propose/idea")
    public String submitProposal(@ModelAttribute Proposal proposal, Principal principal,
            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        proposal.setUser(user);
        proposal.setStatus(ProposalStatus.PENDING);

        proposalRepository.save(proposal);

        redirectAttributes.addFlashAttribute("success", "Your proposal has been submitted successfully!");
        return "redirect:/propose/idea";
    }

    @GetMapping("/proposals/explore")
    public String exploreProposals(Model model) {
        model.addAttribute("proposals", proposalRepository.findByStatus(ProposalStatus.APPROVED));
        return "explore-proposals";
    }

    @GetMapping("/proposals/view/{id}")
    public String viewProposal(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal Id:" + id));
        model.addAttribute("proposal", proposal);
        return "proposal-details";
    }
}
