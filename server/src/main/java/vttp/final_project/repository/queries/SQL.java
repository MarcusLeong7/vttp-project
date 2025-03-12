package vttp.final_project.repository.queries;

public class SQL {

    public static final String SQL_SAVE_USER = "insert into users (email, password) values (?,?)";
    public static final String SQL_FIND_USER_BY_EMAIL = "Select * from users where email = ?";
    public static final String SQL_CHECK_USER_EXISTS = "SELECT COUNT(*) as count FROM users WHERE email = ?";
}
