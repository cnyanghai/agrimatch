package com.agrimatch.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtTokenUtil jwtTokenUtil(
            @Value("${security.jwt.secret:agrimatch-demo-secret-please-change-32bytes}") String secret,
            @Value("${security.jwt.expire-ms:604800000}") long expireMs
    ) {
        return new JwtTokenUtil(secret, expireMs);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenUtil jwtTokenUtil) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.httpBasic(Customizer.withDefaults());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/api/health", "/api/auth/**",
                        "/api/products/tree", "/api/products/search", "/api/products/*/params",
                        "/api/posts", "/api/posts/*/comments").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
        );

        http.addFilterBefore(new JwtAuthFilter(jwtTokenUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


