package com.alden.logmonitoringapp.service;

import com.alden.logmonitoringapp.dto.auth.LoginRequest;
import com.alden.logmonitoringapp.dto.auth.LoginResponse;
import com.alden.logmonitoringapp.dto.auth.RegisterRequest;
import com.alden.logmonitoringapp.dto.auth.RegisterResponse;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.repository.UserAccountRepository;
import com.alden.logmonitoringapp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // used on service classes in Spring
public class UserService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Get userRepository object and get password encoder
    // Constructor injection
    public UserService(UserAccountRepository userAccounts, PasswordEncoder encoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userAccounts;
        this.passwordEncoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {

        // reject if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        // reject if username taken
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        // Create and save user
        User user = new User(request.getEmail(), request.getUsername(), passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user); // .save generates the ID

        // return response
        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
    public LoginResponse  login(LoginRequest request) {
        authenticationManager.authenticate( // authenticate() is from Spring Security's AuthenticationManager interface
                // UsernamePasswordAuthenticationToken is the standard Authentication implementation for username/password login
                // I checked with ctrl + alt + b on the Authenticate object in the manager class
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // credentials are valid if we reach here so we issue a token
        String token = jwtService.generateToken(request.getUsername());
        return new LoginResponse(token);


    }
}
