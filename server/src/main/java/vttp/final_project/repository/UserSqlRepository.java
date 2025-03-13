package vttp.final_project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import vttp.final_project.models.User;

import static vttp.final_project.repository.queries.UserSql.*;


@Repository
public class UserSqlRepository {

    @Autowired
    private JdbcTemplate template;

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
