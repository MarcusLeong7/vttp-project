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
        System.out.println("Updating Google tokens for user: " + email);
        System.out.println("Access token: " + (accessToken != null ? "present" : "null"));
        System.out.println("Refresh token: " + (refreshToken != null ? "present" : "null"));

        // Execute the update
        int rowsUpdated = template.update(SQL_UPDATE_GOOGLE_TOKEN, accessToken, refreshToken, expiryDate, email);
        System.out.println("Rows updated: " + rowsUpdated);

        if (rowsUpdated == 0) {
            System.err.println("Warning: No rows updated when saving Google tokens");
        }
    }

    // Save user into mySQL database
    public void save(User user) {

        template.update(SQL_SAVE_USER, user.getEmail(), user.getPassword(), user.getGoogleAccessToken(),
                user.getGoogleRefreshToken(), user.getGoogleTokenExpiry());
    }

    // Retrieve user by email
    public User findByEmail(String email) {
        SqlRowSet rs = template.queryForRowSet(SQL_FIND_USER_BY_EMAIL, email);
        if (rs.next()) {
            User user = new User();
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPremium(rs.getBoolean("is_premium"));

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

    // Update Premium Status
    public void updatePremiumStatus(String email, boolean isPremium) {
        template.update(SQL_UPDATE_PREMIUM_STATUS, isPremium, email);
    }
}
