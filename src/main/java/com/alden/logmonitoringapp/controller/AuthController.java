package com.alden.logmonitoringapp.controller;

import com.alden.logmonitoringapp.dto.auth.LoginRequest;
import com.alden.logmonitoringapp.dto.auth.LoginResponse;
import com.alden.logmonitoringapp.dto.auth.RegisterRequest;
import com.alden.logmonitoringapp.dto.auth.RegisterResponse;
import com.alden.logmonitoringapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;




// @RestController marks this class as a controller where return values are written directly to the HTTP response as JSON.
// @RequestMapping sets the base path for all endpoints in this class.
@RestController
@RequestMapping("/auth")
public class AuthController {

    // controller depends on the service, not the repository. business logic stays in the service.
    private final UserService userService;

    // Spring injects UserService automatically via constructor injection.
    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")  // Post method
    @ResponseStatus(HttpStatus.CREATED) // sends back HTTP 201 instead of the default 200.
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request); // controller calls service and returns the result.
    }

    @PostMapping("/login")
    // no @ResponseStatus. login returns the default HTTP 200, not 201, because nothing new is created
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);  // credential check and token generation happen in the service
    }
}
