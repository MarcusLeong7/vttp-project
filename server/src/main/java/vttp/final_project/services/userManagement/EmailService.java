package vttp.final_project.services.userManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.subject}")
    private String subject;

    @Async("emailExecutor")
    public void sendWelcomeEmail(String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(buildWelcomeEmailBody(recipientEmail));

        try {
            mailSender.send(message);
            System.out.println("Welcome email sent successfully to: " + recipientEmail);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    // Parameter: User's email Address
    private String buildWelcomeEmailBody(String email) {
        return "Dear " + email + ",\n\n" +
               "Welcome to NutriSense! We're excited to have you join our community.\n\n" +
               "With NutriSense, you can:\n" +
               "- Discover nutritionally balanced meals tailored to your preferences\n" +
               "- Create personalized meal plans\n" +
               "- Track your nutrition goals\n" +
               "- Explore workouts that complement your diet\n\n" +
               "Get started by logging in and exploring our meal catalog.\n\n" +
               "If you have any questions, feel free to contact our support team.\n\n" +
               "Best regards,\n" +
               "The NutriSense Team";
    }
}
