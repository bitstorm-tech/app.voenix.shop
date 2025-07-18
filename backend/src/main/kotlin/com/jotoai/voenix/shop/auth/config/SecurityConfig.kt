package com.jotoai.voenix.shop.auth.config

import com.jotoai.voenix.shop.auth.service.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val customUserDetailsService: CustomUserDetailsService,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(customUserDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager = authConfig.authenticationManager

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }.authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/prompts/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/mugs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/slots/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/images/**")
                    .permitAll()
                    .requestMatchers("/api/openai/**")
                    .permitAll()
                    .requestMatchers("/api/pdf/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/prompts/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/prompts/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/prompts/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/mugs/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/mugs/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/mugs/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/slots/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/slots/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/slots/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/images/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/images/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated()
            }.exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }.authenticationProvider(authenticationProvider())

        return http.build()
    }
}
