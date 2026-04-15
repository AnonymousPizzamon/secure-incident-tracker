package com.alden.logmonitoringapp.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") //change table name to users
public class User {

    protected User() {} //jpa requires no arg constructor


    public User(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    } //Since email will be optional, now we don't have to put null if no email

    @Id //primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto assign value
    private Long id;

    private String email;
    private String username;
    private String passwordHash;
    private LocalDateTime createdAt;


    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return passwordHash;
    }
    public Long getId() {
        return id;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }




}