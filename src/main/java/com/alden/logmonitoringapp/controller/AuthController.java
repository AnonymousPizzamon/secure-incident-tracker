package com.alden.logmonitoringapp.controller;

import com.alden.logmonitoringapp.dto.auth.LoginRequest;
import com.alden.logmonitoringapp.dto.auth.LoginResponse;
import com.alden.logmonitoringapp.dto.auth.RegisterRequest;
import com.alden.logmonitoringapp.dto.auth.RegisterResponse;
import com.alden.logmonitoringapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// @RestController, return values go straight to the response body as json
// @RequestMapping, base path for every endpoint in this class
@RestController
@RequestMapping("/auth")
public class AuthController {

    // depends on the service, not the repository. business logic stays in the service
    private final UserService userService;

    // constructor injection
    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // sends back 201 instead of the default 200
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request); // controller calls service and returns the result
    }

    @PostMapping("/login")
    // no @ResponseStatus. login returns the default HTTP 200, not 201, because nothing new is created
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);  // credential check and token generation happen in the service
    }
}
