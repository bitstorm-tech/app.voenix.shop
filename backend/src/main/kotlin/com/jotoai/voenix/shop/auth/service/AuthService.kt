package com.jotoai.voenix.shop.auth.service

import com.jotoai.voenix.shop.auth.dto.CustomUserDetails
import com.jotoai.voenix.shop.auth.dto.LoginRequest
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.domain.users.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val securityContextRepository: SecurityContextRepository,
    private val userRegistrationService: UserRegistrationService,
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

        // Create new user
        userRegistrationService.createUser(
            email = registerRequest.email,
            password = registerRequest.password,
        )

        // Authenticate the newly registered user
        return userRegistrationService.authenticateUser(
            email = registerRequest.email,
            password = registerRequest.password,
            request = request,
            response = response,
        )
    }

    @Transactional
    fun registerGuest(
        registerGuestRequest: RegisterGuestRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        // Check if email already exists
        val existingUser = userRepository.findByEmail(registerGuestRequest.email).orElse(null)

        if (existingUser != null) {
            // If user exists and has no password, update their details
            if (existingUser.password == null) {
                val updatedUser =
                    userRegistrationService.updateUser(
                        user = existingUser,
                        firstName = registerGuestRequest.firstName,
                        lastName = registerGuestRequest.lastName,
                        phoneNumber = registerGuestRequest.phoneNumber,
                    )

                return userRegistrationService.authenticateGuestUser(
                    user = updatedUser,
                    request = request,
                    response = response,
                )
            } else {
                // User exists with password, cannot register as guest
                throw ResourceAlreadyExistsException("User", "email", registerGuestRequest.email)
            }
        }

        // Create new guest user without password
        val savedUser =
            userRegistrationService.createUser(
                email = registerGuestRequest.email,
                password = null,
                firstName = registerGuestRequest.firstName,
                lastName = registerGuestRequest.lastName,
                phoneNumber = registerGuestRequest.phoneNumber,
            )

        return userRegistrationService.authenticateGuestUser(
            user = savedUser,
            request = request,
            response = response,
        )
    }
}
