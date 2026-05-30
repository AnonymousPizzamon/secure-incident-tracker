package com.alden.logmonitoringapp.dto.auth;



// With JWT authentication, part of it being stateless means all that is returned from a successful login is the JWT.

public class LoginResponse {

    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}