package com.jotoai.voenix.shop.api.auth

import com.jotoai.voenix.shop.auth.dto.LoginRequest
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse = authService.login(loginRequest, request, response)

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest) {
        authService.logout(request)
    }

    @GetMapping("/session")
    fun getSessionInfo(): SessionInfo = authService.getCurrentSession()

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registerRequest: RegisterRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse = authService.register(registerRequest, request, response)

    @PostMapping("/register-guest")
    fun registerGuest(
        @Valid @RequestBody registerGuestRequest: RegisterGuestRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse = authService.registerGuest(registerGuestRequest, request, response)
}
