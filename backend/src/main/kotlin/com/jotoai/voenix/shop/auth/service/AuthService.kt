package com.jotoai.voenix.shop.auth.service

import com.jotoai.voenix.shop.auth.dto.CustomUserDetails
import com.jotoai.voenix.shop.auth.dto.LoginRequest
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.auth.repository.RoleRepository
import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.users.entity.User
import com.jotoai.voenix.shop.domain.users.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val securityContextRepository: SecurityContextRepository,
) {
    @Transactional
    fun login(
        loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        try {
            val authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(
                        loginRequest.email,
                        loginRequest.password,
                    ),
                )

            // Persist the authentication context to the session
            // This ensures the principal is available in subsequent requests
            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = authentication
            SecurityContextHolder.setContext(context)
            securityContextRepository.saveContext(context, request, response)

            val userDetails = authentication.principal as CustomUserDetails
            val user =
                userRepository.findById(userDetails.id).orElseThrow {
                    UsernameNotFoundException("User not found with ID: ${userDetails.id}")
                }
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

    @Transactional(readOnly = true)
    fun getCurrentSession(): SessionInfo {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication.principal is String) {
            return SessionInfo(authenticated = false)
        }

        return when (val principal = authentication.principal) {
            is CustomUserDetails -> {
                val user = userRepository.findById(principal.id).orElse(null)
                if (user != null) {
                    SessionInfo(
                        authenticated = true,
                        user = user.toDto(),
                        roles = user.roles.map { it.name },
                    )
                } else {
                    SessionInfo(authenticated = false)
                }
            }
            else -> SessionInfo(authenticated = false)
        }
    }

    @Transactional
    fun register(
        registerRequest: RegisterRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.email)) {
            throw ResourceAlreadyExistsException("User", "email", registerRequest.email)
        }

        // Find USER role
        val userRole =
            roleRepository.findByName("USER").orElseThrow {
                ResourceNotFoundException("Role", "name", "USER")
            }

        // Create new user
        val user =
            User(
                email = registerRequest.email,
                password = passwordEncoder.encode(registerRequest.password),
                roles = setOf(userRole),
            )

        val savedUser = userRepository.save(user)

        // Authenticate the newly registered user
        val authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    registerRequest.email,
                    registerRequest.password,
                ),
            )

        // Create session
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
        securityContextRepository.saveContext(context, request, response)

        val session = request.getSession(true)

        return LoginResponse(
            user = savedUser.toDto(),
            sessionId = session.id,
            roles = savedUser.roles.map { it.name },
        )
    }
}
