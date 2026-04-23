package com.alden.logmonitoringapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Provides info to Service class which builds User
public class RegisterRequest {

    @NotBlank(message = "Username required")
    private String username;

    @NotBlank(message = "Password required")
    @Size(min = 8, message = "Needs to be at least 8 characters")
    private String password;

    @Email(message = "Must be a valid email address")
    private String email; // optional so no @NotBlank

    public RegisterRequest() { //required by JPA which creates empty object that is filled by the incoming HTTP JSON
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
}
