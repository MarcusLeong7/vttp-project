package vttp.final_project.repository.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import vttp.final_project.models.userModels.UserHealthData;

import static vttp.final_project.repository.queries.UserHealthSql.*;

@Repository
public class UserHealthRepository {

    @Autowired
    private JdbcTemplate template;

    // Save user health data (either insert new or update existing)
    public void saveUserHealthData(UserHealthData healthData) {
        // Check if record exists
        SqlRowSet rs = template.queryForRowSet(SQL_CHECK_USER_HEALTH_EXISTS, healthData.getUserId());
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt("count") > 0;
        }

        if (exists) {
            // Update existing record
            template.update(SQL_UPDATE_USER_HEALTH,
                    healthData.getHeight(),
                    healthData.getWeight(),
                    healthData.getAge(),
                    healthData.getGender(),
                    healthData.getActivityLevel(),
                    healthData.getFitnessGoal(),
                    healthData.getBmi(),
                    healthData.getBmr(),
                    healthData.getTdee(),
                    healthData.getUserId());
        } else {
            // Insert new record
            template.update(SQL_SAVE_USER_HEALTH,
                    healthData.getUserId(),
                    healthData.getHeight(),
                    healthData.getWeight(),
                    healthData.getAge(),
                    healthData.getGender(),
                    healthData.getActivityLevel(),
                    healthData.getFitnessGoal(),
                    healthData.getBmi(),
                    healthData.getBmr(),
                    healthData.getTdee());
        }
    }

    // Find health data by user ID
    public UserHealthData findByUserId(Integer userId) {
        SqlRowSet rs = template.queryForRowSet(SQL_FIND_USER_HEALTH_BY_USER_ID, userId);
        if (rs.next()) {
            UserHealthData healthData = new UserHealthData();
            healthData.setId(rs.getInt("id"));
            healthData.setUserId(rs.getInt("user_id"));
            healthData.setHeight(rs.getBigDecimal("height"));
            healthData.setWeight(rs.getBigDecimal("weight"));
            healthData.setAge(rs.getInt("age"));
            healthData.setGender(rs.getString("gender"));
            healthData.setActivityLevel(rs.getString("activity_level"));
            healthData.setFitnessGoal(rs.getString("fitness_goal"));
            healthData.setBmi(rs.getBigDecimal("bmi"));
            healthData.setBmr(rs.getInt("bmr"));
            healthData.setTdee(rs.getInt("tdee"));
            return healthData;
        }
        return null;
    }

    // Delete user health data
    public boolean deleteUserHealthData(Integer userId) {
        int rowsAffected = template.update(SQL_DELETE_USER_HEALTH, userId);
        return rowsAffected > 0;
    }

    // Check if health data exists for user
    public boolean existsByUserId(Integer userId) {
        SqlRowSet rs = template.queryForRowSet(SQL_CHECK_USER_HEALTH_EXISTS, userId);
        if (rs.next()) {
            return rs.getInt("count") > 0;
        }
        return false;
    }
}
