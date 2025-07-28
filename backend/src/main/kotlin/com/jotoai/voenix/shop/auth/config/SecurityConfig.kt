package com.jotoai.voenix.shop.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager = authConfig.authenticationManager

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .securityContext { context ->
                context.securityContextRepository(securityContextRepository())
            }.csrf { it.disable() }
            .cors { }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }.authorizeHttpRequests { auth ->
                auth
                    // Static resources and frontend assets
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/assets/**",
                        "/images/**",
                        "/*.js",
                        "/*.css",
                        "/*.ico",
                        "/*.png",
                        "/*.jpg",
                        "/*.svg",
                        "/*.webp",
                        "/*.woff",
                        "/*.woff2",
                        "/*.ttf",
                        "/*.eot",
                    ).permitAll()
                    // Auth endpoints - No authentication required
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    // Public API endpoints for e-commerce functionality
                    .requestMatchers(
                        "/api/prompts/**",
                        "/api/mugs/**",
                        "/api/categories/**",
                        "/api/subcategories/**",
                        "/api/public/**",
                    ).permitAll()
                    // Public service endpoints
                    .requestMatchers(
                        "/api/openai/images/edit",
                        "/api/pdf/generate",
                    ).permitAll()
                    // Admin endpoints - Require ADMIN role
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    // User endpoints - Require authentication
                    .requestMatchers("/api/user/**")
                    .authenticated()
                    // Frontend routes (for SPA routing)
                    .requestMatchers("/login", "/editor", "/admin/**")
                    .permitAll()
                    // Deny all other requests by default
                    .anyRequest()
                    .authenticated()
            }.exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }.build()
}
