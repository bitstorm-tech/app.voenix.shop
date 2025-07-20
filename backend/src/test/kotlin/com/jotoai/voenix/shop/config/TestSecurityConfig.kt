package com.jotoai.voenix.shop.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@TestConfiguration
class TestSecurityConfig {
    @Bean
    @Primary
    fun testPasswordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
