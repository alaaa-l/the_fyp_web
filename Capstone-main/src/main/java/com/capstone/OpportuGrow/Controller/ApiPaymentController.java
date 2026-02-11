package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Dto.PaymentIntentResponseDto;
import com.capstone.OpportuGrow.Dto.PaymentRequestDto;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.Service.StripeService;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.User;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class ApiPaymentController {

    private final StripeService stripeService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentRequestDto requestDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            Project project = projectRepository.findById(requestDto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));

            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String currency = requestDto.getCurrency() != null ? requestDto.getCurrency() : "usd";

            PaymentIntent intent = stripeService.createPaymentIntent(
                    requestDto.getAmount(),
                    currency,
                    project.getId().toString(),
                    String.valueOf(user.getId()),
                    project.getType().toString());

            PaymentIntentResponseDto response = PaymentIntentResponseDto.builder()
                    .clientSecret(intent.getClientSecret())
                    .publishableKey(stripeService.getPublicKey())
                    .paymentIntentId(intent.getId())
                    .projectId(project.getId())
                    .amount(requestDto.getAmount())
                    .build();

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.internalServerError().body("Stripe Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
