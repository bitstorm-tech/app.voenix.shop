package com.jotoai.voenix.shop.api.public.auth

import com.jotoai.voenix.shop.auth.dto.LoginRequest
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/auth")
class PublicAuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest,
    ): LoginResponse = authService.login(loginRequest, request)

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest) {
        authService.logout(request)
    }

    @GetMapping("/session")
    fun getSessionInfo(): SessionInfo = authService.getCurrentSession()
}
