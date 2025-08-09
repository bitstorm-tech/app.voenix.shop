package com.jotoai.voenix.shop.auth.api

import com.jotoai.voenix.shop.auth.api.dto.SessionInfo

/**
 * Query service for authentication-related read operations.
 */
interface AuthQueryService {
    /**
     * Gets the current session information for the authenticated user.
     */
    fun getCurrentSession(): SessionInfo
}
