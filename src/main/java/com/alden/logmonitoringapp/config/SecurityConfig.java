package com.alden.logmonitoringapp.config;

import com.alden.logmonitoringapp.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration //needed when Bean definition is used
public class SecurityConfig {


    // constructor injection for JwtAuthFilter
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // Bean tells Spring to call this method once at startup, and register the object it returns in the application context.
    // Any other class that needs a PasswordEncoder can then inject it with @Autowired or constructor injection (better).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    } // return type is PasswordEncoder interface because BCryptPasswordEncoder implements the PasswordEncoder interface


    // AuthenticationManager needs two beans to work: AppUserDetailsService (loads the user),
    // and PasswordEncoder (checks the password).
    // config.getAuthenticationManager() finds both,
    // and assembles them into the finished AuthenticationManager that UserService injects for login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    // SecurityFilterChain defines the HTTP security rules for the app.
    // Spring Security applies these rules to every incoming request.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // disable CSRF — not needed for a stateless REST API using JWT tokens.
            .csrf(csrf -> csrf.disable())

            // stateless session — Spring will not create or use HTTP sessions. identity comes from the JWT on each request.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // define which endpoints are public and which require authentication.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register", "/auth/login", "/error" ).permitAll() //public endpoints
                .anyRequest().authenticated()// everything else requires a valid token
            )
            // run the JWT filter before Spring's default username/password filter so the SecurityContext
            // is already populated from the token by the time Spring checks if the request is authenticated.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
        // reference:
        // https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html#authorize-requests
    }
}

