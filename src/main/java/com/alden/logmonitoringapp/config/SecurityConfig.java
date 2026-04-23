package com.alden.logmonitoringapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration //needed when Bean definition is used
public class SecurityConfig {

    //Bean tells Spring to call this method once at startup, and register the object it returns in the application context.
    //Any other class that needs a PasswordEncoder can then inject it with @Autowired or constructor injection (better).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} //return type is PasswordEncoder interface because BCryptPasswordEncoder implements the PasswordEncoder interface
