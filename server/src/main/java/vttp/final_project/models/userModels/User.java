package vttp.final_project.models.userModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class User {

    @Pattern(
            regexp = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-z]{2,}$",
            message = "Invalid email format. Please include a valid domain (e.g., .com, .org)."
    )
    @Email(message = "Username needs to be an email!")
    @NotBlank(message = "Email required")
    private String email;
    @NotBlank (message= "Password required!")
    @Pattern.List({
            @Pattern(
                    regexp = ".*[A-Z].*",
                    message = "Password must include an uppercase character."
            ),
            @Pattern(
                    regexp = ".*[a-z].*",
                    message = "Password must include a lowercase character."
            ),
            @Pattern(
                    regexp = ".*\\d.*",
                    message = "Password must include at least one digit."
            ),
            @Pattern(
                    regexp = ".*[@$!%*?&].*",
                    message = "Password must include at least one special character."
            ),
            @Pattern(
                    regexp = ".{8,}",
                    message = "Password must be at least 8 characters long."
            )
    })
    private String password;

    public User() {
    }

    public User(String email, String password) {
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

    @Override
    public String toString() {
        return "User{" +
               "email='" + email + '\'' +
               '}';
    }
}
