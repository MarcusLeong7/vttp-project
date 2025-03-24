package vttp.final_project.controller;


import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping("/config")
    public ResponseEntity<String> getPublicKey() {
        JsonObject response = Json.createObjectBuilder()
                .add("publicKey", stripeService.getPublicKey())
                .build();
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckoutSession(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        String priceId = (String) payload.get("priceId");

        // Determine the price based on the selected plan
        String priceName = "premium".equals(priceId) ? "Monthly Premium" : "Annual Premium";
        long amount = "premium".equals(priceId) ? 999 : 1999;  // $9.99 or $19.99

        try {
            // Create checkout session
            Session session = stripeService.createCheckoutSession(
                    email,
                    amount,
                    priceName,
                    frontendUrl + "/payment/success",
                    frontendUrl + "/payment/cancel"
            );

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "success")
                    .add("sessionId", session.getId())
                    .add("url", session.getUrl())
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

    @GetMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestParam("session_id") String sessionId, Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        try {
            String email = principal.getName();
            boolean verified = stripeService.verifyPaymentSuccess(sessionId);

            if (verified) {
                // Update user to premium status in database
                userSqlRepo.updatePremiumStatus(email, true);

                JsonObject response = Json.createObjectBuilder()
                        .add("status", "success")
                        .add("message", "Payment successful and premium status updated")
                        .build();

                return ResponseEntity.ok(response.toString());
            } else {
                JsonObject error = Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Payment verification failed")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.toString());
            }
        } catch (StripeException e) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.toString());
        }
    }


}
