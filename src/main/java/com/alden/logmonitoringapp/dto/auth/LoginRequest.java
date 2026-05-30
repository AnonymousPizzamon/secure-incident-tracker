package com.alden.logmonitoringapp.dto.auth;

import jakarta.validation.constraints.NotBlank;


// It's good to separate login from request dto's so they can be modified individually and because they are separate processes.

public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
