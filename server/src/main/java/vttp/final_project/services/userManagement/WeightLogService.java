package vttp.final_project.services.userManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vttp.final_project.models.userModels.WeightLog;
import vttp.final_project.repository.user.UserSqlRepository;
import vttp.final_project.repository.user.WeightLogRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class WeightLogService {

    @Autowired
    private WeightLogRepository weightLogRepo;

    @Autowired
    private UserSqlRepository userSqlRepo;

    // Get weight logs for a user for the last X days
    public List<WeightLog> getWeightLogs(String email, int days) {
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            throw new RuntimeException("User not found: " + email);
        }

        LocalDate startDate = LocalDate.now().minusDays(days);
        return weightLogRepo.getWeightLogs(userId, startDate);
    }

    // Add a new weight log or update existing for the current day
    public WeightLog addWeightLog(String email, BigDecimal weight, String notes) {
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            throw new RuntimeException("User not found: " + email);
        }

        LocalDate today = LocalDate.now();
        // Check if there's already a log for today
        WeightLog existingLog = weightLogRepo.getWeightLogForDate(userId, today);

        if (existingLog != null) {
            // Update existing log
            existingLog.setWeight(weight);
            existingLog.setNotes(notes);
            weightLogRepo.updateWeightLog(existingLog);
            return existingLog;
        } else {
            // Create a new log
            WeightLog newLog = new WeightLog();
            newLog.setUserId(userId);
            newLog.setWeight(weight);
            newLog.setDate(today);
            newLog.setNotes(notes);
            return weightLogRepo.saveWeightLog(newLog);
        }
    }

    // Update an existing weight log
    public WeightLog updateWeightLog(String email, Long logId, BigDecimal weight, String notes) {
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            throw new RuntimeException("User not found: " + email);
        }
        WeightLog log = weightLogRepo.getWeightLogById(logId, userId);
        if (log == null) {
            throw new RuntimeException("Weight log not found");
        }
        log.setWeight(weight);
        log.setNotes(notes);
        boolean updated = weightLogRepo.updateWeightLog(log);
        if (!updated) {
            throw new RuntimeException("Failed to update weight log");
        }

        return log;
    }

    // Delete a weight log
    public boolean deleteWeightLog(String email, Long logId) {
        Integer userId = userSqlRepo.getUserIdByEmail(email);
        if (userId == null) {
            throw new RuntimeException("User not found: " + email);
        }
        return weightLogRepo.deleteWeightLog(logId, userId);
    }


}
