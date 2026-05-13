package com.ai.main.config;

import com.ai.main.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요
                        .requestMatchers("/v1/auth/**").permitAll()
                        // OpenAPI / Swagger UI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // ADMIN 전용 — 비회원 GET 허용 매처보다 먼저 와야 함 (/v1/deals/* 와 충돌)
                        .requestMatchers(HttpMethod.GET, "/v1/deals/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/orders/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/orders/*/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/coupons").hasRole("ADMIN")
                        // 상품/카테고리/타임딜 조회는 비회원도 가능
                        .requestMatchers(HttpMethod.GET,
                                "/v1/products/**",
                                "/v1/categories/**",
                                "/v1/deals/active",
                                "/v1/deals/upcoming",
                                "/v1/deals/*").permitAll()
                        // 주문 상태 변경은 ADMIN 전용
                        .requestMatchers(HttpMethod.PATCH, "/v1/orders/*/status").hasRole("ADMIN")
                        // text2SQL 쿼리 기능은 ADMIN만 사용 가능
                        .requestMatchers("/v1/query/**").hasRole("ADMIN")
                        // 그 외 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            String reason = (String) request.getAttribute(com.ai.main.security.JwtAuthFilter.AUTH_ERROR_ATTR);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String message;
            if (com.ai.main.security.JwtAuthFilter.ERR_EXPIRED.equals(reason)) {
                message = "토큰이 만료되었습니다.";
            } else if (com.ai.main.security.JwtAuthFilter.ERR_REVOKED.equals(reason)) {
                message = "로그아웃된 토큰입니다.";
            } else if (com.ai.main.security.JwtAuthFilter.ERR_INVALID.equals(reason)) {
                message = "유효하지 않은 토큰입니다.";
            } else {
                message = "인증이 필요합니다.";
            }
            if (reason != null) {
                response.setHeader("X-Auth-Error", reason);
            }
            response.getWriter().write("{\"message\":\"" + message + "\"}");
        };
    }
}