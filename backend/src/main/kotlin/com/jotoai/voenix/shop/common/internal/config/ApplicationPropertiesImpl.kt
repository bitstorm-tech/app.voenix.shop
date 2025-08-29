package com.jotoai.voenix.shop.common.internal.config

import com.jotoai.voenix.shop.common.api.config.ApplicationConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
data class ApplicationPropertiesImpl(
    var testMode: Boolean = false,
    @param:Value("\${spring.profiles.active:default}") private val activeProfile: String = "default",
) : ApplicationConfiguration {
    override fun getActiveProfile(): String = activeProfile

    override fun isProduction(): Boolean = activeProfile == "prod"

    override fun isDevelopment(): Boolean = !isProduction()
}
