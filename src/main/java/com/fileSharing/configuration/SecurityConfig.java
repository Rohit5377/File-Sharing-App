package com.fileSharing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/files/home", "/files/download/*", "/files/share/*", "/styles/**").permitAll();
            auth.anyRequest().authenticated();
        })
                .oauth2Login(oauth2Login -> oauth2Login.loginPage("/oauth2/authorization/google")
                        .defaultSuccessUrl("/files/list"))
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL to trigger logout
                        .logoutSuccessUrl("/files/home") // Redirect after logout
                        .invalidateHttpSession(true) // Invalidate session
                )
                .csrf(csrf -> csrf.disable());
        return httpSecurity.build();
    }

}
