package vttp.final_project.controller.userControllers;


import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    /* This is an endpoint to test if JWT token authentication works */
    /* In postman headers tab:
     * Key: Authorization
     * Value: Bearer "token_string" */
    @GetMapping("/profile")
    public ResponseEntity<String> getUserProfile(Principal principal) {
        if (principal == null) {
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.toString());
        }

        String email = principal.getName();
        JsonObject response = Json.createObjectBuilder()
                .add("email", email)
                .add("message", "This is a protected endpoint that requires JWT authentication")
                .build();

        return ResponseEntity.ok(response.toString());
    }
}
