package com.jotoai.voenix.shop.auth.api

import com.jotoai.voenix.shop.auth.api.dto.LoginRequest
import com.jotoai.voenix.shop.auth.api.dto.LoginResponse
import com.jotoai.voenix.shop.auth.api.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.api.dto.SessionInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Unified authentication service providing all authentication, registration, and session operations.
 * This service consolidates previously separated CQRS interfaces into a single cohesive interface.
 */
interface AuthService {
    /**
     * Authenticates a user with email and password.
     */
    fun login(
        loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse

    /**
     * Gets the current session information for the authenticated user.
     */
    fun getCurrentSession(): SessionInfo

    /**
     * Registers a new user with email and password.
     */
    fun register(
        registerRequest: RegisterRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse

    /**
     * Registers a guest user without password authentication.
     */
    fun registerGuest(
        registerGuestRequest: RegisterGuestRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse

}
