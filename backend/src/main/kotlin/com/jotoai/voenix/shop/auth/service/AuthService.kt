package com.jotoai.voenix.shop.auth.service

import com.jotoai.voenix.shop.auth.dto.LoginRequest
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.users.entity.User
import com.jotoai.voenix.shop.users.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun login(
        loginRequest: LoginRequest,
        request: HttpServletRequest,
    ): LoginResponse {
        try {
            val authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(
                        loginRequest.email,
                        loginRequest.password,
                    ),
                )

            SecurityContextHolder.getContext().authentication = authentication

            val user = authentication.principal as User
            val session = request.getSession(true)

            return LoginResponse(
                user = user.toDto(),
                sessionId = session.id,
                roles = user.roles.map { it.name },
            )
        } catch (_: BadCredentialsException) {
            throw BadCredentialsException("Invalid email or password")
        } catch (_: UsernameNotFoundException) {
            throw BadCredentialsException("Invalid email or password")
        }
    }

    fun logout(request: HttpServletRequest) {
        SecurityContextHolder.clearContext()
        request.getSession(false)?.invalidate()
    }

    fun getCurrentSession(): SessionInfo {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            return SessionInfo(authenticated = false)
        }

        return when (val principal = authentication.principal) {
            is User -> {
                SessionInfo(
                    authenticated = true,
                    user = principal.toDto(),
                    roles = principal.roles.map { it.name },
                )
            }
            else -> SessionInfo(authenticated = false)
        }
    }
}
