package com.jotoai.voenix.shop.auth.api

import com.jotoai.voenix.shop.auth.api.dto.LoginRequest
import com.jotoai.voenix.shop.auth.api.dto.LoginResponse
import com.jotoai.voenix.shop.auth.api.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Main authentication facade providing login, registration, and logout operations.
 */
interface AuthFacade {
    /**
     * Authenticates a user with email and password.
     */
    fun login(
        loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse

    /**
     * Logs out the current user and invalidates the session.
     */
    fun logout(request: HttpServletRequest)

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
