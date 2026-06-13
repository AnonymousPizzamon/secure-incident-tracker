package com.alden.logmonitoringapp.security;

// JwtService can generate and validate tokens but nothing can verify a username and password yet
// Spring Security's AuthenticationManager needs a UserDetailsService to do that during login
// AuthenticationManager will use this to load users during login — we will later wire this in SecurityConfig
// https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html

import java.util.Collections;
import com.alden.logmonitoringapp.entity.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.alden.logmonitoringapp.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userRepository;

    public AppUserDetailsService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Fetch the user from the database by username and hand it back to Spring Security with the stored password hash.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User: " + username + " not found!"));

        // Spring Security's built-in User builder converts our entity into a UserDetails object
        // fully qualified name because our entity is also called User
        // passwordHash is already BCrypt-encoded — Spring will compare it against the raw login input
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // establishes role, no roles in this app so .emptylist()
                .build();
    }
}