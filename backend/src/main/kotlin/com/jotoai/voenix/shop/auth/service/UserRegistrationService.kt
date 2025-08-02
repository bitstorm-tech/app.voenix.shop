package com.jotoai.voenix.shop.auth.service

import com.jotoai.voenix.shop.auth.dto.CustomUserDetails
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.entity.Role
import com.jotoai.voenix.shop.auth.repository.RoleRepository
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.users.entity.User
import com.jotoai.voenix.shop.domain.users.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
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
    ): User {
        val roles = findRolesByNames(roleNames)

        val user =
            User(
                email = email,
                password = password?.let { passwordEncoder.encode(it) },
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                roles = roles,
            )

        return userRepository.save(user)
    }

    /**
     * Updates an existing user's details
     */
    @Transactional
    fun updateUser(
        user: User,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
    ): User {
        firstName?.let { user.firstName = it }
        lastName?.let { user.lastName = it }
        phoneNumber?.let { user.phoneNumber = it }

        return userRepository.save(user)
    }

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
        user: User,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        val userDetails =
            CustomUserDetails(
                id = user.id!!,
                email = user.email,
                passwordHash = null,
                userRoles = user.roles.map { it.name }.toSet(),
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
        val user =
            userRepository.findById(userDetails.id).orElseThrow {
                ResourceNotFoundException("User", "id", userDetails.id.toString())
            }
        val session = request.getSession(true)

        return LoginResponse(
            user = user.toDto(),
            sessionId = session.id,
            roles = user.roles.map { it.name },
        )
    }

    /**
     * Finds roles by their names
     */
    private fun findRolesByNames(roleNames: Set<String>): Set<Role> =
        roleNames
            .map { roleName ->
                roleRepository.findByName(roleName).orElseThrow {
                    ResourceNotFoundException("Role", "name", roleName)
                }
            }.toSet()
}
