package vttp.final_project.repository.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import vttp.final_project.models.userModels.WeightLog;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static vttp.final_project.repository.queries.WeightLogSql.*;

@Repository
public class WeightLogRepository {

    @Autowired
    private JdbcTemplate template;

    // Save a new weight log
    public WeightLog saveWeightLog(WeightLog log) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_WEIGHT_LOG, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, log.getUserId());
            ps.setBigDecimal(2, log.getWeight());
            ps.setDate(3, Date.valueOf(log.getDate()));
            ps.setString(4, log.getNotes());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            log.setId(generatedId.longValue());
        }

        return log;
    }

    // Get weight logs for a user within a date range using SqlRowSet
    public List<WeightLog> getWeightLogs(Integer userId, LocalDate startDate) {
        List<WeightLog> logs = new ArrayList<>();

        SqlRowSet rs = template.queryForRowSet(SQL_GET_WEIGHT_LOGS,
                userId,
                Date.valueOf(startDate));

        while (rs.next()) {
            logs.add(mapRowToWeightLog(rs));
        }
        return logs;
    }

    // Get a specific weight log by ID using SqlRowSet
    public WeightLog getWeightLogById(Long id, Integer userId) {
        SqlRowSet rs = template.queryForRowSet(SQL_GET_WEIGHT_LOG_BY_ID, id, userId);

        if (rs.next()) {
            return mapRowToWeightLog(rs);
        }
        return null;
    }

    // Check if a log already exists for a date using SqlRowSet
    public WeightLog getWeightLogForDate(Integer userId, LocalDate date) {
        SqlRowSet rs = template.queryForRowSet(SQL_GET_WEIGHT_LOG_FOR_DATE,
                userId,
                Date.valueOf(date));

        if (rs.next()) {
            return mapRowToWeightLog(rs);
        }

        return null;
    }

    // Update an existing weight log using JdbcTemplate
    public boolean updateWeightLog(WeightLog log) {
        // Use JdbcTemplate's update method with direct parameters
        int rowsUpdated = template.update(SQL_UPDATE_WEIGHT_LOG,
                log.getWeight(),
                log.getNotes(),
                log.getId(),
                log.getUserId());

        return rowsUpdated > 0;
    }

    // Delete a weight log using JdbcTemplate
    public boolean deleteWeightLog(Long id, Integer userId) {
        // Use JdbcTemplate's update method with direct parameters
        int rowsDeleted = template.update(SQL_DELETE_WEIGHT_LOG, id, userId);

        return rowsDeleted > 0;
    }

    // Helper method to map SqlRowSet to WeightLog object
    private WeightLog mapRowToWeightLog(SqlRowSet rs) {
        WeightLog log = new WeightLog();
        log.setId(rs.getLong("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setWeight(rs.getBigDecimal("weight"));
        log.setDate(rs.getDate("date").toLocalDate());
        log.setNotes(rs.getString("notes"));
        return log;
    }
}
