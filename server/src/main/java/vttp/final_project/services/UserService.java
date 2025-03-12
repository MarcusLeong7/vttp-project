package vttp.final_project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vttp.final_project.models.User;
import vttp.final_project.repository.UserRepository;
import vttp.final_project.repository.UserSqlRepository;

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

    public void registerUser(String email, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(rawPassword)); // Hash the password

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


}
