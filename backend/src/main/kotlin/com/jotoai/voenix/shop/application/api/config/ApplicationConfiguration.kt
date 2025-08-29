package com.jotoai.voenix.shop.application.api.config

/**
 * Interface for application-wide configuration properties.
 * Provides access to common configuration settings across modules.
 */
interface ApplicationConfiguration {
    /**
     * Gets the active Spring profile
     */
    fun getActiveProfile(): String

    /**
     * Checks if the application is running in production mode
     */
    fun isProduction(): Boolean

    /**
     * Checks if the application is running in development mode
     */
    fun isDevelopment(): Boolean
}
