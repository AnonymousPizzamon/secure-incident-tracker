package com.alden.logmonitoringapp.service;

import com.alden.logmonitoringapp.dto.auth.RegisterRequest;
import com.alden.logmonitoringapp.dto.auth.RegisterResponse;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service //user on service classes in Spring
public class UserService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //Get userRepository object and get password encoder
    //Constructor injection
    public UserService(UserAccountRepository userAccounts, PasswordEncoder encoder) {
        this.userRepository = userAccounts;
        this.passwordEncoder = encoder;
    }

    public RegisterResponse register(RegisterRequest request) {

        //reject if username taken
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already in use");
        }
        //Create and save user
        User user = new User(request.getEmail(), request.getUsername(), passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user); //.save generates the ID

        //return response
        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }
}
