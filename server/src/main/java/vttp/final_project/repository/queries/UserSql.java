package vttp.final_project.repository.queries;

public class UserSql {

    public static final String SQL_SAVE_USER =
            "insert into users (email, password, google_access_token, google_refresh_token, google_token_expiry) " +
                                               "values (?,?,?,?,?)";
    public static final String SQL_FIND_USER_BY_EMAIL = "Select * from users where email = ?";
    public static final String SQL_CHECK_USER_EXISTS = "SELECT COUNT(*) as count FROM users WHERE email = ?";
    public static final String SQL_UPDATE_GOOGLE_TOKEN = "UPDATE users SET google_access_token = ?, google_refresh_token = ?, google_token_expiry = ? WHERE email = ?";
}
