package vttp.final_project.models.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginUser {

    @Pattern(
            regexp = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-z]{2,}$",
            message = "Invalid email format. Please include a valid domain (e.g., .com, .org)."
    )
    @Email(message = "Username needs to be an email!")
    @NotBlank(message = "Email required")
    private String email;
    @NotBlank(message = "Password required!")
    private String password;


    public LoginUser() {
    }

    public LoginUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
