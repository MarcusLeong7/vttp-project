package vttp.final_project.repository.queries;

public class UserHealthSql {

    public static final String SQL_SAVE_USER_HEALTH =
            "INSERT INTO user_health_data (user_id, height, weight, age, gender, activity_level, fitness_goal, bmi, bmr, tdee) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_UPDATE_USER_HEALTH =
            "UPDATE user_health_data SET height = ?, weight = ?, age = ?, gender = ?, " +
            "activity_level = ?, fitness_goal = ?, bmi = ?, bmr = ?, tdee = ? WHERE user_id = ?";

    public static final String SQL_FIND_USER_HEALTH_BY_USER_ID =
            "SELECT * FROM user_health_data WHERE user_id = ?";

    public static final String SQL_DELETE_USER_HEALTH =
            "DELETE FROM user_health_data WHERE user_id = ?";

    public static final String SQL_CHECK_USER_HEALTH_EXISTS =
            "SELECT COUNT(*) as count FROM user_health_data WHERE user_id = ?";
}
