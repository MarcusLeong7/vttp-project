package vttp.final_project.services.userManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vttp.final_project.models.userModels.UserHealthData;
import vttp.final_project.repository.user.UserHealthRepository;
import vttp.final_project.repository.user.UserSqlRepository;

@Service
public class UserHealthService {

    @Autowired
    private UserHealthRepository userHealthRepo;

    @Autowired
    private UserSqlRepository userSqlRepo;

    // Get health data for a user
    public UserHealthData getUserHealthData(String email) {
        // Get user ID from email
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            return null;
        }
        return userHealthRepo.findByUserId(userId);
    }

    // Save or update health data
    public UserHealthData saveUserHealthData(String email, UserHealthData healthData) {
        // Get user ID from email
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Set user ID in health data
        healthData.setUserId(userId);

        // Calculate BMI, BMR, and TDEE
        healthData.calculateBmi();
        healthData.calculateBmr();
        healthData.calculateTdee();

        // Save to database
        userHealthRepo.saveUserHealthData(healthData);

        return healthData;
    }

    // Delete health data
    public boolean deleteUserHealthData(String email) {
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            return false;
        }
        return userHealthRepo.deleteUserHealthData(userId);
    }

    // Check if user has health data
    public boolean userHasHealthData(String email) {
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            return false;
        }
        return userHealthRepo.existsByUserId(userId);
    }
}
