package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.TransactionDto;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Transaction;
import com.capstone.OpportuGrow.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiTransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/transactions")
    public ResponseEntity<?> getMyTransactions(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        List<Transaction> transactions = transactionRepository.findBySender(user);

        List<TransactionDto> dtos = transactions.stream().map(t -> TransactionDto.builder()
                .id(t.getId())
                .projectId(t.getProject() != null ? t.getProject().getId() : null)
                .projectTitle(t.getProject() != null ? t.getProject().getTitle() : "Unknown Project")
                .amount(t.getAmount() != null ? t.getAmount() : 0.0)
                .type(t.getType() != null ? t.getType() : "PAYMENT") // Use type from model, default to PAYMENT
                .timestamp(t.getTimestamp())
                .status("COMPLETED") // Assume completed if in transaction table
                .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
