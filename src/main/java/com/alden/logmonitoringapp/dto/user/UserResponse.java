package com.alden.logmonitoringapp.dto.user;

import java.time.LocalDateTime;

// dto for GET /users/me
// gets returned by UserController
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;

    public UserResponse(Long id, String username, String email, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
