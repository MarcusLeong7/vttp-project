package vttp.final_project.controller.userControllers;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vttp.final_project.configuration.jwtToken.JwtUtil;
import vttp.final_project.models.userModels.LoginUser;
import vttp.final_project.models.userModels.User;
import vttp.final_project.services.userManagement.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthLoginController {

    @Autowired
    private UserService userSvc;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(path="/register",consumes = MediaType.APPLICATION_JSON_VALUE
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        // Check if user already exists
        if (userSvc.existsByEmail(user.getEmail())) {
            JsonObject userExists = Json.createObjectBuilder()
                    .add("status","success")
                    .add("message","Email is already registered")
                    .build();
            // Error: 409 Response code
            return ResponseEntity.status(HttpStatus.CONFLICT).body(userExists.toString());
        }
        try {
            // Register the user
            userSvc.registerUser(user.getEmail(), user.getPassword());
            JsonObject successResponse = Json.createObjectBuilder()
                    .add("status","success")
                    .add("message","User successfully registered!")
                    .build();
            // 201 created resource -> User
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse.toString());
        } catch (Exception e) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("status","success")
                    .add("message","Registration failed: " + e.getMessage())
                    .build();
            // Error 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse.toString());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginUser loginUser) {
        try {
            boolean authenticated = userSvc.authenticate(loginUser.getEmail(), loginUser.getPassword());

            if (authenticated) {
                // Generate JWT token
                String token = jwtUtil.generateToken(loginUser.getEmail());
                System.out.println("Token:" + token);

                JsonObject resp = Json.createObjectBuilder()
                        .add("status","success")
                        .add("message","Login successful!")
                        .add("email",loginUser.getEmail())
                        .add("token",token)
                        .build();

                return ResponseEntity.ok(resp.toString());
            } else {
                JsonObject resp = Json.createObjectBuilder()
                        .add("status","error")
                        .add("message","Invalid email or password!")
                        .build();
                // 401 Error Code
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp.toString());
            }
        } catch (Exception e) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("status","error")
                    .add("message","Login failed: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse.toString());
        }
    }


    // We can add more auth-related endpoints here later, such as:
    // - /api/auth/logout
    // - /api/auth/refresh-token
    // - /api/auth/forgot-password
    // - /api/auth/reset-password
}
