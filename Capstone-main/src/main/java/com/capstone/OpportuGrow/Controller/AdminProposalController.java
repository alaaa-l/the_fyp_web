package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Repository.ProposalRepository;
import com.capstone.OpportuGrow.model.Proposal;
import com.capstone.OpportuGrow.model.ProposalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/proposals")
@RequiredArgsConstructor
public class AdminProposalController {

    private final ProposalRepository proposalRepository;

    @GetMapping
    public String getAllProposals(Model model) {
        List<Proposal> proposals = proposalRepository.findAll();
        model.addAttribute("proposals", proposals);
        model.addAttribute("pageTitle", "Manage Proposals");
        model.addAttribute("contentTemplate", "admin-proposals");
        model.addAttribute("activePage", "proposals");
        return "admin-layout";
    }

    @GetMapping("/{id}")
    public String getProposalDetails(@PathVariable Long id, Model model) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal Id:" + id));

        model.addAttribute("proposal", proposal);
        model.addAttribute("pageTitle", "Proposal Details");
        model.addAttribute("contentTemplate", "admin-proposal-details");
        model.addAttribute("activePage", "proposals");
        return "admin-layout";
    }

    @PostMapping("/approve/{id}")
    public String approveProposal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal Id:" + id));

        proposal.setStatus(ProposalStatus.APPROVED);
        proposalRepository.save(proposal);

        redirectAttributes.addFlashAttribute("successMessage", "Proposal approved successfully!");
        return "redirect:/admin/proposals/" + id;
    }

    @PostMapping("/reject/{id}")
    public String rejectProposal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal Id:" + id));

        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);

        redirectAttributes.addFlashAttribute("successMessage", "Proposal rejected successfully.");
        return "redirect:/admin/proposals/" + id;
    }
}
