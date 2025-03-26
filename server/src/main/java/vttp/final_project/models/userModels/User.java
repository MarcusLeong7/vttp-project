package vttp.final_project.models.userModels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Date;

public class User {

    private String email;
    private String password;
    private Boolean isPremium;
    private String googleAccessToken;
    private String googleRefreshToken;
    private Date googleTokenExpiry;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, Boolean isPremium,
                String googleAccessToken, String googleRefreshToken, Date googleTokenExpiry) {
        this.email = email;
        this.password = password;
        this.isPremium = isPremium;
        this.googleAccessToken = googleAccessToken;
        this.googleRefreshToken = googleRefreshToken;
        this.googleTokenExpiry = googleTokenExpiry;
    }

    public Boolean isPremium() {
        return isPremium;
    }

    public void setPremium(Boolean premium) {
        isPremium = premium;
    }

    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }

    public String getGoogleRefreshToken() {
        return googleRefreshToken;
    }

    public void setGoogleRefreshToken(String googleRefreshToken) {
        this.googleRefreshToken = googleRefreshToken;
    }

    public Date getGoogleTokenExpiry() {
        return googleTokenExpiry;
    }

    public void setGoogleTokenExpiry(Date googleTokenExpiry) {
        this.googleTokenExpiry = googleTokenExpiry;
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
