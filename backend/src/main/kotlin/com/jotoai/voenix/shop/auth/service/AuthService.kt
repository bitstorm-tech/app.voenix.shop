package com.jotoai.voenix.shop.auth.service

import com.jotoai.voenix.shop.auth.dto.CustomUserDetails
import com.jotoai.voenix.shop.auth.dto.LoginRequest
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.user.api.UserAuthenticationService
import com.jotoai.voenix.shop.user.api.UserQueryService
import com.jotoai.voenix.shop.user.api.UserRoleManagementService
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
    private val userAuthenticationService: UserAuthenticationService,
    private val userQueryService: UserQueryService,
    private val userRoleManagementService: UserRoleManagementService,
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
            val userDto = userQueryService.getUserById(userDetails.id)
            val userRoles = userRoleManagementService.getUserRoles(userDetails.id)
            val session = request.getSession(true)

            return LoginResponse(
                user = userDto,
                sessionId = session.id,
                roles = userRoles.toList(),
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
                try {
                    val userDto = userQueryService.getUserById(principal.id)
                    val userRoles = userRoleManagementService.getUserRoles(principal.id)

                    SessionInfo(
                        authenticated = true,
                        user = userDto,
                        roles = userRoles.toList(),
                    )
                } catch (e: Exception) {
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
        if (userQueryService.existsByEmail(registerRequest.email)) {
            throw ResourceAlreadyExistsException("User", "email", registerRequest.email)
        }
        userRegistrationService.createUser(
            email = registerRequest.email,
            password = registerRequest.password,
        )
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
        // Check if user already exists
        val existsUser = userQueryService.existsByEmail(registerGuestRequest.email)

        if (existsUser) {
            try {
                val existingUser = userQueryService.getUserByEmail(registerGuestRequest.email)
                // Try to get authentication info to see if they have a password
                val authInfo = userAuthenticationService.loadUserByEmail(registerGuestRequest.email)

                // If user exists and has no password, update their details
                if (authInfo?.passwordHash == null) {
                    userRegistrationService.updateUser(
                        userId = existingUser.id,
                        firstName = registerGuestRequest.firstName,
                        lastName = registerGuestRequest.lastName,
                        phoneNumber = registerGuestRequest.phoneNumber,
                    )

                    return userRegistrationService.authenticateGuestUser(
                        userId = existingUser.id,
                        request = request,
                        response = response,
                    )
                } else {
                    // User exists with password, cannot register as guest
                    throw ResourceAlreadyExistsException("User", "email", registerGuestRequest.email)
                }
            } catch (e: Exception) {
                // User exists with password, cannot register as guest
                throw ResourceAlreadyExistsException("User", "email", registerGuestRequest.email)
            }
        }
        val savedUser =
            userRegistrationService.createUser(
                email = registerGuestRequest.email,
                password = null,
                firstName = registerGuestRequest.firstName,
                lastName = registerGuestRequest.lastName,
                phoneNumber = registerGuestRequest.phoneNumber,
            )

        return userRegistrationService.authenticateGuestUser(
            userId = savedUser.id,
            request = request,
            response = response,
        )
    }
}
