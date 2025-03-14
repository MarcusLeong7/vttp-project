package vttp.final_project.repository.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import vttp.final_project.models.userModels.User;

import java.util.Date;

import static vttp.final_project.repository.queries.UserSql.*;


@Repository
public class UserSqlRepository {

    @Autowired
    private JdbcTemplate template;

    // Add Google token update method
    public void updateGoogleTokens(String email, String accessToken, String refreshToken, Date expiryDate) {
        // Define the SQL update statement
        String sql = "UPDATE users SET google_access_token = ?, google_refresh_token = ?, google_token_expiry = ? WHERE email = ?";

        // Execute the update
        template.update(sql, accessToken, refreshToken, expiryDate, email);
    }

    // Save user into mySQL database
    public void save(User user) {
        template.update(SQL_SAVE_USER, user.getEmail(), user.getPassword());
    }

    // Retrieve user by email
    public User findByEmail(String email) {
        SqlRowSet rs = template.queryForRowSet(SQL_FIND_USER_BY_EMAIL, email);
        if (rs.next()) {
            User user = new User();
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));

            // Add Google token fields
            user.setGoogleAccessToken(rs.getString("google_access_token"));
            user.setGoogleRefreshToken(rs.getString("google_refresh_token"));

            // Handle date conversion for token expiry
            java.sql.Timestamp expiryTimestamp = rs.getTimestamp("google_token_expiry");
            if (expiryTimestamp != null) {
                user.setGoogleTokenExpiry(new Date(expiryTimestamp.getTime()));
            }

            return user;
        }
        return null;
    }

    // Check if user exists
    public boolean existsByEmail(String email) {
        SqlRowSet rs = template.queryForRowSet(SQL_CHECK_USER_EXISTS, email);
        if (rs.next()) {
            return rs.getInt("count") > 0;
        }
        return false;
    }
}
