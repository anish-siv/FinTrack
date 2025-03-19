package com.finance.expensetracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabling CSRF protection
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/css/**", "/js/**", "/images/**").permitAll() // Allowing access to static resources
                .requestMatchers("/api/auth/**").permitAll() // Allowing access to authentication endpoints
                .anyRequest().authenticated() // Requiring authentication for all other requests
            )
            .formLogin(form -> form
                .loginPage("/login") // Login page
                .loginProcessingUrl("/api/auth/login") // Login processing URL
                .successHandler(loginSuccessHandler()) // Success handler
                .permitAll() // Allowing access to login page
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // Logout URL
                .logoutSuccessUrl("/") // Logout success URL
                .permitAll() // Allowing access to logout
            );
        
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() { // Login success handler
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/expenses");
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { // Authentication manager
        return config.getAuthenticationManager();
    }
} 