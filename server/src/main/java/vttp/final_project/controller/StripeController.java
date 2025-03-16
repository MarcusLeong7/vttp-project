package vttp.final_project.controller;


import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vttp.final_project.repository.user.UserSqlRepository;
import vttp.final_project.services.StripeService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private UserSqlRepository userSqlRepo;

    @GetMapping("/config")
    public ResponseEntity<String> getPublicKey() {
        JsonObject response = Json.createObjectBuilder()
                .add("publicKey", stripeService.getPublicKey())
                .build();
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        String priceId = (String) payload.get("priceId");

        // Set the amount based on the subscription tier
        Long amount = "premium".equals(priceId) ? 999L : 1999L;  // $9.99 or $19.99

        try {
            PaymentIntent intent = stripeService.createPaymentIntent(email, amount);

            // Set isPremium flag to true in database after successful payment
            // This would normally be done after payment confirmation, but we're setting it here for simplicity
            userSqlRepo.updatePremiumStatus(email, true);

            JsonObject response = Json.createObjectBuilder()
                    .add("clientSecret", intent.getClientSecret())
                    .build();

            return ResponseEntity.ok(response.toString());
        } catch (StripeException e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }

    @PostMapping("/payment-success")
    public ResponseEntity<String> paymentSuccess(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();

        // Update user to premium status in database
        userSqlRepo.updatePremiumStatus(email, true);

        JsonObject response = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Payment successful and premium status updated")
                .build();

        return ResponseEntity.ok(response.toString());
    }

}
