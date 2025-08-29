package com.jotoai.voenix.shop.auth.internal.service

import com.jotoai.voenix.shop.auth.api.AuthService
import com.jotoai.voenix.shop.auth.api.dto.LoginRequest
import com.jotoai.voenix.shop.auth.api.dto.LoginResponse
import com.jotoai.voenix.shop.auth.api.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.api.dto.SessionInfo
import com.jotoai.voenix.shop.auth.api.dto.UserCreationRequest
import com.jotoai.voenix.shop.auth.api.exceptions.InvalidCredentialsException
import com.jotoai.voenix.shop.auth.internal.security.CustomUserDetails
import com.jotoai.voenix.shop.common.api.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.user.api.UserService
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthServiceImpl(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val securityContextRepository: SecurityContextRepository,
    private val passwordEncoder: PasswordEncoder,
) : AuthService {
    @Transactional
    override fun login(
        loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        try {
            return authenticateUser(
                email = loginRequest.email,
                password = loginRequest.password,
                request = request,
                response = response,
            )
        } catch (_: BadCredentialsException) {
            throw InvalidCredentialsException()
        } catch (_: UsernameNotFoundException) {
            throw InvalidCredentialsException()
        }
    }

    /**
     * Fetches user data and roles in a consolidated manner.
     * Note: Consider optimizing to a single query once UserQueryService supports it.
     */
    private fun fetchUserWithRoles(userId: Long): Pair<UserDto, Set<String>> {
        // Currently makes 2 queries - should be optimized to 1 query with JOIN
        val userDto = userService.getUserById(userId)
        val userRoles = userService.getUserRoles(userId)
        return Pair(userDto, userRoles)
    }

    @Transactional(readOnly = true)
    override fun getCurrentSession(): SessionInfo {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication.principal is String) {
            return SessionInfo(authenticated = false)
        }

        return when (val principal = authentication.principal) {
            is CustomUserDetails -> {
                try {
                    // Optimized: Fetch user and roles together (still 2 queries but consolidated)
                    val (userDto, userRoles) = fetchUserWithRoles(principal.id)

                    SessionInfo(
                        authenticated = true,
                        user = userDto,
                        roles = userRoles.toList(),
                    )
                } catch (_: ResourceNotFoundException) {
                    SessionInfo(authenticated = false)
                }
            }
            else -> SessionInfo(authenticated = false)
        }
    }

    @Transactional
    override fun register(
        registerRequest: RegisterRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        if (userService.existsByEmail(registerRequest.email)) {
            throw ResourceAlreadyExistsException("User", "email", registerRequest.email)
        }
        val userDto =
            createUser(
                UserCreationRequest(
                    email = registerRequest.email,
                    password = registerRequest.password,
                ),
            )

        // Create authentication directly using the newly created user data
        // to avoid database lookup issues with transaction boundaries
        return authenticateRegisteredUser(
            userId = userDto.id,
            request = request,
            response = response,
        )
    }

    @Transactional
    override fun registerGuest(
        registerGuestRequest: RegisterGuestRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        val email = registerGuestRequest.email
        val authInfo = userService.loadUserByEmail(email)

        if (authInfo != null) {
            if (authInfo.passwordHash == null) {
                // Update profile fields for existing guest
                updateUser(
                    userId = authInfo.id,
                    firstName = registerGuestRequest.firstName,
                    lastName = registerGuestRequest.lastName,
                    phoneNumber = registerGuestRequest.phoneNumber,
                )
                return authenticateGuestUser(
                    userId = authInfo.id,
                    request = request,
                    response = response,
                )
            } else {
                throw ResourceAlreadyExistsException("User", "email", email)
            }
        }

        val savedUser =
            createUser(
                UserCreationRequest(
                    email = email,
                    password = null,
                    firstName = registerGuestRequest.firstName,
                    lastName = registerGuestRequest.lastName,
                    phoneNumber = registerGuestRequest.phoneNumber,
                ),
            )

        return authenticateGuestUser(
            userId = savedUser.id,
            request = request,
            response = response,
        )
    }

    @Transactional
    override fun createUser(request: UserCreationRequest): UserDto {
        // Create user via unified service
        val userDto =
            userService.createUser(
                CreateUserRequest(
                    email = request.email,
                    firstName = request.firstName,
                    lastName = request.lastName,
                    phoneNumber = request.phoneNumber,
                    password = request.password?.let { passwordEncoder.encode(it) },
                ),
            )

        // Assign roles using the unified service
        if (request.roleNames.isNotEmpty()) {
            userService.setUserRoles(userDto.id, request.roleNames)
        }

        return userDto
    }

    @Transactional
    override fun updateUser(
        userId: Long,
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
    ): UserDto =
        userService.updateUser(
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
    private fun authenticateUser(
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
    private fun authenticateGuestUser(
        userId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        val (userDto, userRoles) = fetchUserWithRoles(userId)

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
     * Creates an authenticated session for a newly registered user (without password authentication)
     * This avoids database lookup issues when authenticating immediately after user creation
     */
    private fun authenticateRegisteredUser(
        userId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): LoginResponse {
        val (userDto, userRoles) = fetchUserWithRoles(userId)

        val userDetails =
            CustomUserDetails(
                id = userDto.id,
                email = userDto.email,
                passwordHash = "registered", // Not used for authentication token, just a marker
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
        authentication: Authentication,
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
        // Optimized: Fetch user and roles together (still 2 queries but consolidated)
        val (userDto, userRoles) = fetchUserWithRoles(userDetails.id)
        val session = request.getSession(true)
        // Protect against session fixation by rotating the session ID
        request.changeSessionId()
        val sessionId = request.getSession(false)?.id ?: session.id

        return LoginResponse(
            user = userDto,
            sessionId = sessionId,
            roles = userRoles.toList(),
        )
    }
}
