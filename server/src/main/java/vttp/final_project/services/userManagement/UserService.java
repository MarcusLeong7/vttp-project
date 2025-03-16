package vttp.final_project.services.userManagement;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vttp.final_project.models.userModels.User;
import vttp.final_project.repository.user.UserRepository;
import vttp.final_project.repository.user.UserSqlRepository;

import java.io.IOException;
import java.util.Date;

@Service
public class UserService {

    // Redis Repo
    @Autowired
    private UserRepository userRepo;

    // SQL Repo
    @Autowired
    private UserSqlRepository userSqlRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailSvc;

    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.client.secret}")
    private String clientSecret;
    @Value("${google.redirect.uri}")
    private String redirectUri;

    public void registerUser(String email, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(rawPassword)); // Hash the password

        // Set premium status to false for new users
        user.setPremium(false);
        // Initialize Google fields as null for new users
        user.setGoogleAccessToken(null);
        user.setGoogleRefreshToken(null);
        user.setGoogleTokenExpiry(null);

        // Save to databases
        userSqlRepo.save(user);
        userRepo.save(user);

        // Send welcome email asynchronously
        System.out.println("Sending email to: " + email);
        emailSvc.sendWelcomeEmail(email);
    }

    public boolean authenticate(String email, String rawPassword) {
        System.out.println(">>> authenticate triggered for email: " + email);

        // Try SQL first
        User sqlUser = userSqlRepo.findByEmail(email);
        if (sqlUser != null) {
            System.out.println(" From mySQL:");
            System.out.println("Raw password: " + rawPassword);
            System.out.println("Stored hashed password in SQL: " + sqlUser.getPassword());
            return passwordEncoder.matches(rawPassword, sqlUser.getPassword());
        }
        // Fallback to redis
        User user = userRepo.findByEmail(email);
        if (user == null) {
            System.out.println("User not found for email: " + email);
            return false;} // User not found
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Stored hashed password: " + user.getPassword());
        // verify password during login
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // Check if user exists by email
    public boolean existsByEmail(String email) {
        return userSqlRepo.existsByEmail(email);
    }

    // Random key for health check
    public String checkHealth() {
        String randomKey = userRepo.getRandomKey();
        if (randomKey == null) {
            return "Redis Health Check: Unhealthy (No keys found or Redis is down)";
        }
        return "Redis Health Check: Healthy (Random Key: " + randomKey + ")";
    }

    public void saveGoogleAuthToken(String email, String authCode) {
        try {
            // Exchange auth code for tokens
            TokenResponse tokenResponse = exchangeAuthCodeForTokens(authCode);

            // Extract tokens and expiry
            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();

            // Important! Log to see what's being received
            System.out.println("Access token received: " + (accessToken != null));
            System.out.println("Refresh token received: " + (refreshToken != null));

            if (refreshToken == null) {
                System.out.println("WARNING: No refresh token received. User might have already granted access before.");
                // You may need to handle the case where no refresh token is returned
            }

            Date expiryDate = new Date(System.currentTimeMillis() + (tokenResponse.getExpiresInSeconds() * 1000));

            // Update user with Google tokens
            userSqlRepo.updateGoogleTokens(email, accessToken, refreshToken, expiryDate);

            // Verify the update was successful
            User updatedUser = userSqlRepo.findByEmail(email);
            System.out.println("After update - User has refresh token: " + (updatedUser.getGoogleRefreshToken() != null));

        } catch (Exception e) {
            System.err.println("Error saving Google auth token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process Google authentication", e);
        }
    }

    private TokenResponse exchangeAuthCodeForTokens(String authCode) {
        try {
            System.out.println("Exchanging auth code for tokens");

            // Create GoogleTokenResponse from authCode
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    authCode,
                    redirectUri)  // Must match exactly what's in Google Cloud Console
                    .execute();

            System.out.println("Token exchange completed");
            return tokenResponse;
        } catch (IOException e) {
            System.err.println("Failed to exchange auth code for tokens: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to exchange auth code for tokens", e);
        }
    }


}
