package com.jotoai.voenix.shop.auth.controller

import com.jotoai.voenix.shop.auth.dto.LoginRequest
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    ): LoginResponse {
        return authService.login(loginRequest, request, response)
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        authService.logout(request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/session")
    fun getCurrentSession(): ResponseEntity<SessionInfo> {
        val sessionInfo = authService.getCurrentSession()
        return ResponseEntity.ok(sessionInfo)
    }
}
