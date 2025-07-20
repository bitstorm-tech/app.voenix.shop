package com.jotoai.voenix.shop.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles

@TestConfiguration
@ActiveProfiles("test")
@EntityScan(basePackages = ["com.jotoai.voenix.shop"])
@EnableJpaRepositories(basePackages = ["com.jotoai.voenix.shop"])
@ComponentScan(basePackages = ["com.jotoai.voenix.shop"])
class TestDataConfig
