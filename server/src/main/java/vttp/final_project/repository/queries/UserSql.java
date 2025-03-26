package vttp.final_project.repository.queries;

public class UserSql {

    public static final String SQL_SAVE_USER =
            "insert into users (email, password,is_premium, google_access_token, google_refresh_token, google_token_expiry) " +
                                               "values (?,?,?,?,?,?)";
    public static final String SQL_FIND_USER_BY_EMAIL = "select * from users where email = ?";
    public static final String SQL_CHECK_USER_EXISTS = "select count(*) as count from users where email = ?";
    public static final String SQL_UPDATE_GOOGLE_TOKEN = "update users set google_access_token = ?, google_refresh_token = ?, google_token_expiry = ? WHERE email = ?";
    public static final String SQL_UPDATE_PREMIUM_STATUS = "update users set is_premium = ? where email = ?";
    public static final String SQL_GET_USERID = "select id from users where email = ?";
}
