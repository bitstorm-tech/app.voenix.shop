package com.jotoai.voenix.shop.auth.internal.service

import com.jotoai.voenix.shop.auth.api.AuthFacade
import com.jotoai.voenix.shop.auth.api.dto.LoginRequest
import com.jotoai.voenix.shop.auth.api.dto.LoginResponse
import com.jotoai.voenix.shop.auth.api.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthFacadeImpl(
    private val authService: AuthService,
) : AuthFacade {
    @Transactional
    override fun login(
        loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse = authService.login(loginRequest, request, response)

    override fun logout(request: HttpServletRequest) {
        authService.logout(request)
    }

    @Transactional
    override fun register(
        registerRequest: RegisterRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse = authService.register(registerRequest, request, response)

    @Transactional
    override fun registerGuest(
        registerGuestRequest: RegisterGuestRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse = authService.registerGuest(registerGuestRequest, request, response)
}
