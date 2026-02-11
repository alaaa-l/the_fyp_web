package com.capstone.OpportuGrow.Controller;

import com.capstone.OpportuGrow.Config.StripeConfig;
import com.capstone.OpportuGrow.Repository.ProjectRepository;
import com.capstone.OpportuGrow.Repository.TransactionRepository;
import com.capstone.OpportuGrow.Repository.UserRepository;
import com.capstone.OpportuGrow.model.Project;
import com.capstone.OpportuGrow.model.ProjectStatus;
import com.capstone.OpportuGrow.model.Transaction;
import com.capstone.OpportuGrow.model.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/stripe")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeConfig stripeConfig;
    private final TransactionRepository transactionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public StripeWebhookController(StripeConfig stripeConfig, TransactionRepository transactionRepository,
            ProjectRepository projectRepository, UserRepository userRepository) {
        this.stripeConfig = stripeConfig;
        this.transactionRepository = transactionRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        String payload;
        try {
            byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
            payload = new String(body, StandardCharsets.UTF_8);

            if (payload == null || payload.isEmpty()) {
                logger.error("Empty payload received.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Empty payload");
            }
        } catch (Exception e) {
            logger.error("Failed to read request body: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid body");
        }

        Event event;
        try {
            String webhookSecret = stripeConfig.getWebhookSecret();
            if (webhookSecret != null)
                webhookSecret = webhookSecret.trim();
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.error("Signature Verification Failed for event: {}", sigHeader);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Webhook construction error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Process Error");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            try {
                // Fallback for API version mismatches (e.g., experimental versions)
                stripeObject = dataObjectDeserializer.deserializeUnsafe();
            } catch (Exception e) {
                logger.error("Deserialization failed definitively for event {}: {}", event.getId(), e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Deserialization failed");
            }
        }

        if ("payment_intent.succeeded".equals(event.getType()) && stripeObject instanceof PaymentIntent) {
            handlePaymentIntentSucceeded((PaymentIntent) stripeObject);
        }

        return ResponseEntity.ok("Received");
    }

    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();

        if (transactionRepository.existsByPaymentIntentId(paymentIntentId)) {
            return;
        }

        Map<String, String> metadata = paymentIntent.getMetadata();
        String projectIdStr = metadata.get("projectId");
        String userIdStr = metadata.get("userId");
        String pType = metadata.get("projectType");

        if (projectIdStr == null || userIdStr == null) {
            logger.warn("Received success webhook without projectId/userId in metadata.");
            return;
        }

        try {
            Long projectId = Long.valueOf(projectIdStr);
            Integer userId = Integer.valueOf(userIdStr);
            Double amount = paymentIntent.getAmount() / 100.0;

            Optional<Project> pOpt = projectRepository.findById(projectId);
            Optional<User> uOpt = userRepository.findById(userId);

            if (pOpt.isPresent() && uOpt.isPresent()) {
                Project project = pOpt.get();
                User user = uOpt.get();

                project.setRaisedAmount(project.getRaisedAmount() + amount);
                if (project.getRaisedAmount() >= project.getFundingGoal()) {
                    project.setStatus(ProjectStatus.COMPLETED);
                }
                projectRepository.save(project);

                Transaction t = new Transaction();
                t.setSender(user);
                t.setProject(project);
                t.setAmount(amount);
                t.setTimestamp(LocalDateTime.now());
                t.setType(pType != null ? pType : "UNKNOWN");
                t.setPaymentIntentId(paymentIntentId);
                transactionRepository.save(t);

                logger.info("Successfully processed payment for project {}. New raisedAmount: {}", projectId,
                        project.getRaisedAmount());
            } else {
                logger.error("Project ({}) or User ({}) missing from database during webhook processing.", projectId,
                        userId);
            }
        } catch (Exception e) {
            logger.error("Error processing succeeded payment intent {}: {}", paymentIntentId, e.getMessage());
        }
    }
}
