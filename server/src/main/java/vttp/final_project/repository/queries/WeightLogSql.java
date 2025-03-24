package vttp.final_project.repository.queries;

public class WeightLogSql {

    public static final String SQL_INSERT_WEIGHT_LOG =
            "INSERT INTO weight_logs (user_id, weight, date, notes) VALUES (?, ?, ?, ?)";

    public static final String SQL_GET_WEIGHT_LOGS =
            "SELECT * FROM weight_logs WHERE user_id = ? AND date >= ? ORDER BY date DESC";

    public static final String SQL_GET_WEIGHT_LOG_BY_ID =
            "SELECT * FROM weight_logs WHERE id = ? AND user_id = ?";

    public static final String SQL_UPDATE_WEIGHT_LOG =
            "UPDATE weight_logs SET weight = ?, notes = ? WHERE id = ? AND user_id = ?";

    public static final String SQL_DELETE_WEIGHT_LOG =
            "DELETE FROM weight_logs WHERE id = ? AND user_id = ?";

    public static final String SQL_GET_WEIGHT_LOG_FOR_DATE =
            "SELECT * FROM weight_logs WHERE user_id = ? AND date = ?";

}
