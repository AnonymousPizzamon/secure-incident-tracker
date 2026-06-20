package com.alden.logmonitoringapp.controller;

import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.dto.user.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import com.alden.logmonitoringapp.security.CurrentUserProvider;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// We never accept a user id from the client. identity comes from the token via
// CurrentUserProvider. Standard Spring MVC controller that delegates and maps to a DTO.
// Reference: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html

@RestController
@RequestMapping("/users")
public class UserController {

    private final CurrentUserProvider currentUserProvider;

    public UserController(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    public UserResponse me() {
        User user = currentUserProvider.getCurrentUser();
        // entity maps to DTO so we never expose the password hash or other internal fields
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}



