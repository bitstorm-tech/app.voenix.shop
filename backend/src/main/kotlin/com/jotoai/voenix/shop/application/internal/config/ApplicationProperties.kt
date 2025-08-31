package com.jotoai.voenix.shop.application.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
data class ApplicationProperties(
    var testMode: Boolean = false,
)
