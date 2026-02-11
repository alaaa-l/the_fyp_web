package com.capstone.OpportuGrow.Service;

import com.capstone.OpportuGrow.Config.StripeConfig;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService {

    private final StripeConfig stripeConfig;

    public StripeService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }

    @PostConstruct
    public void init() {
        if (stripeConfig.getSecretKey() == null || stripeConfig.getSecretKey().trim().isEmpty()) {
            System.err.println("STRIPE ERROR: Secret Key is missing in configuration!");
        } else {
            Stripe.apiKey = stripeConfig.getSecretKey().trim();
        }
    }

    public PaymentIntent createPaymentIntent(Double amount, String currency, String projectId, String userId,
            String projectType) throws StripeException {
        // Fallback init if needed
        if (Stripe.apiKey == null || Stripe.apiKey.isEmpty()) {
            Stripe.apiKey = stripeConfig.getSecretKey();
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount * 100)) // Stripe expects amount in cents
                .setCurrency(currency)
                .putMetadata("projectId", projectId)
                .putMetadata("userId", userId)
                .putMetadata("projectType", projectType)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent;
    }

    public String getPublicKey() {
        return stripeConfig.getPublicKey() != null ? stripeConfig.getPublicKey().trim() : null;
    }
}
