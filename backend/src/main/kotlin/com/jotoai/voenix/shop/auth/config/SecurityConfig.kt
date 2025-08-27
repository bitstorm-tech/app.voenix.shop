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
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

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
                    // Auth endpoints - No authentication required
                    .requestMatchers("/api/auth/**").permitAll()
                    // Public API endpoints for e-commerce functionality
                    .requestMatchers(
                        "/api/prompts/**",
                        "/api/mugs/**",
                        "/api/categories/**",
                        "/api/subcategories/**",
                        "/api/public/**",
                        "/api/openai/images/edit",
                        "/api/pdf/generate",
                    ).permitAll()
                    // Admin endpoints - Require ADMIN role
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // User endpoints - Require authentication
                    .requestMatchers("/api/user/**").authenticated()
                    // Any other API endpoints require authentication by default
                    .requestMatchers("/api/**").authenticated()
                    // Non-API requests (frontend assets and routes) are public
                    .anyRequest().permitAll()
            }.exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }.logout { logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
            }.build()
}
