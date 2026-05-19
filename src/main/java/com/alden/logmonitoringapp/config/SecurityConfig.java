package com.alden.logmonitoringapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration //needed when Bean definition is used
public class SecurityConfig {

    //Bean tells Spring to call this method once at startup, and register the object it returns in the application context.
    //Any other class that needs a PasswordEncoder can then inject it with @Autowired or constructor injection (better).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    } //return type is PasswordEncoder interface because BCryptPasswordEncoder implements the PasswordEncoder interface

    //SecurityFilterChain defines the HTTP security rules for the app.
    //Spring Security applies these rules to every incoming request.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //disable CSRF — not needed for a stateless REST API using JWT tokens.
            .csrf(csrf -> csrf.disable())

            //stateless session — Spring will not create or use HTTP sessions. identity comes from the JWT on each request.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            //define which endpoints are public and which require authentication.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register", "/auth/login").permitAll() //public endpoints
                .anyRequest().authenticated()                                  //everything else requires a valid token
            );

        return http.build();
        // reference:
        // https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html#authorize-requests
    }
}

