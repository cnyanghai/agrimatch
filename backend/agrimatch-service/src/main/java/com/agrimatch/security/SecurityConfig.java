package com.agrimatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtTokenUtil jwtTokenUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expire-ms:604800000}") long expireMs
    ) {
        return new JwtTokenUtil(secret, expireMs);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenUtil jwtTokenUtil) throws Exception {
        http.cors(Customizer.withDefaults()); // 让Spring Security放行CORS预检请求
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.httpBasic(basic -> basic.disable());

        // 未认证时返回JSON 401，不触发浏览器Basic Auth弹窗
        http.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            new ObjectMapper().writeValue(response.getOutputStream(),
                    Map.of("code", 401, "message", "未登录"));
        }));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/api/health", "/api/config", "/api/auth/**",
                        "/api/products/tree", "/api/products/search", "/api/products/*/params",
                        "/api/product-schemas", "/api/product-schemas/**",
                        "/api/posts", "/api/posts/*", "/api/posts/*/comments",
                        "/api/supplies", "/api/supplies/*",
                        "/api/requirements", "/api/requirements/*",
                        "/api/companies/top", "/api/companies/suppliers", "/api/companies/buyers",
                        "/api/companies/*/profile", "/api/companies/directory",
                        "/api/search/**",
                        "/api/home/stats",
                        "/api/futures/**",
                        "/api/files/download", "/uploads/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
        );

        http.addFilterBefore(new JwtAuthFilter(jwtTokenUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
