package com.alden.logmonitoringapp.dto.auth;

import jakarta.validation.constraints.NotBlank;


//login + registration are two separate processes, so we will keep the dtos separate as well

public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
