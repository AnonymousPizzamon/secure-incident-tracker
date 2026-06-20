package com.alden.logmonitoringapp.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.repository.UserAccountRepository;
import org.springframework.stereotype.Component;

// This class is used as a helper to get info about the current user through the JWT

@Component
public class CurrentUserProvider {

    private final UserAccountRepository userRepository;

    public CurrentUserProvider(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Only used on protected routes, so Spring Security has already authenticated the request.
    // Gets the name of the user from the JWT then loads user from db or throws exception.
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // getName() returns the JWTs username
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
    }
}
