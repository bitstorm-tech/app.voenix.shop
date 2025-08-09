package com.jotoai.voenix.shop.auth.internal.service

import com.jotoai.voenix.shop.auth.api.dto.LoginResponse
import com.jotoai.voenix.shop.auth.internal.security.CustomUserDetails
import com.jotoai.voenix.shop.user.api.UserFacade
import com.jotoai.voenix.shop.user.api.UserPasswordService
import com.jotoai.voenix.shop.user.api.UserQueryService
import com.jotoai.voenix.shop.user.api.UserRoleManagementService
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRegistrationService(
    private val userFacade: UserFacade,
    private val userQueryService: UserQueryService,
    private val userRoleManagementService: UserRoleManagementService,
    private val userPasswordService: UserPasswordService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val securityContextRepository: SecurityContextRepository,
) {
    /**
     * Creates a new user with the specified details and roles
     */
    @Transactional
    fun createUser(
        email: String,
        password: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        roleNames: Set<String> = setOf("USER"),
    ): UserDto {
        // Create user via facade
        val userDto =
            userFacade.createUser(
                CreateUserRequest(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    password = password?.let { passwordEncoder.encode(it) },
                ),
            )

        // Assign roles using the role management service
        if (roleNames.isNotEmpty()) {
            userRoleManagementService.setUserRoles(userDto.id, roleNames)
        }

        return userDto
    }

    /**
     * Updates an existing user's details
     */
    @Transactional
    fun updateUser(
        userId: Long,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
    ): UserDto =
        userFacade.updateUser(
            userId,
            UpdateUserRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
            ),
        )

    /**
     * Authenticates a user with email and password using Spring Security
     */
    fun authenticateUser(
        email: String,
        password: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        val authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, password),
            )

        return createAuthenticatedSession(authentication, request, response)
    }

    /**
     * Creates an authenticated session for a guest user (without password authentication)
     */
    fun authenticateGuestUser(
        userId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        val userDto = userQueryService.getUserById(userId)
        val userRoles = userRoleManagementService.getUserRoles(userId)

        val userDetails =
            CustomUserDetails(
                id = userDto.id,
                email = userDto.email,
                passwordHash = null,
                userRoles = userRoles,
            )

        val authentication =
            UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities,
            )

        return createAuthenticatedSession(authentication, request, response)
    }

    /**
     * Creates and persists an authenticated session
     */
    private fun createAuthenticatedSession(
        authentication: org.springframework.security.core.Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        // Create and persist security context
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
        securityContextRepository.saveContext(context, request, response)

        // Get user details and create session
        val userDetails = authentication.principal as CustomUserDetails
        val userDto = userQueryService.getUserById(userDetails.id)
        val userRoles = userRoleManagementService.getUserRoles(userDetails.id)
        val session = request.getSession(true)

        return LoginResponse(
            user = userDto,
            sessionId = session.id,
            roles = userRoles.toList(),
        )
    }
}
