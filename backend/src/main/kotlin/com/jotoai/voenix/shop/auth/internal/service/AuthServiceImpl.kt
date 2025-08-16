package com.jotoai.voenix.shop.auth.internal.service

import com.jotoai.voenix.shop.auth.api.AuthService
import com.jotoai.voenix.shop.auth.api.dto.LoginRequest
import com.jotoai.voenix.shop.auth.api.dto.LoginResponse
import com.jotoai.voenix.shop.auth.api.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.api.dto.SessionInfo
import com.jotoai.voenix.shop.auth.internal.security.CustomUserDetails
import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
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
            val userDto = userService.getUserById(userDetails.id)
            val userRoles = userService.getUserRoles(userDetails.id)
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

    @Transactional
    override fun logout(request: HttpServletRequest) {
        // Clear security context from thread-local storage
        SecurityContextHolder.clearContext()

        // Invalidate the HTTP session if it exists
        request.getSession(false)?.invalidate()

        // Note: SecurityContextRepository clearing needs HttpServletResponse
        // which is not available in current method signature.
        // This would require updating the interface to include response parameter.
    }

    /**
     * Fetches user data and roles in a consolidated manner
     * TODO: This should be optimized to a single query when UserQueryService supports it
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
        createUser(
            email = registerRequest.email,
            password = registerRequest.password,
        )
        return authenticateUser(
            email = registerRequest.email,
            password = registerRequest.password,
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
        // Check if user already exists
        val existsUser = userService.existsByEmail(registerGuestRequest.email)

        if (existsUser) {
            try {
                val existingUser = userService.getUserByEmail(registerGuestRequest.email)
                // Try to get authentication info to see if they have a password
                val authInfo = userService.loadUserByEmail(registerGuestRequest.email)

                // If user exists and has no password, update their details
                if (authInfo?.passwordHash == null) {
                    updateUser(
                        userId = existingUser.id,
                        firstName = registerGuestRequest.firstName,
                        lastName = registerGuestRequest.lastName,
                        phoneNumber = registerGuestRequest.phoneNumber,
                    )

                    return authenticateGuestUser(
                        userId = existingUser.id,
                        request = request,
                        response = response,
                    )
                } else {
                    // User exists with password, cannot register as guest
                    throw ResourceAlreadyExistsException("User", "email", registerGuestRequest.email)
                }
            } catch (_: ResourceNotFoundException) {
                // User does not exist, proceed to create guest user
            } catch (_: BadCredentialsException) {
                // User exists with password, cannot register as guest
                throw ResourceAlreadyExistsException("User", "email", registerGuestRequest.email)
            }
        }
        val savedUser =
            createUser(
                email = registerGuestRequest.email,
                password = null,
                firstName = registerGuestRequest.firstName,
                lastName = registerGuestRequest.lastName,
                phoneNumber = registerGuestRequest.phoneNumber,
            )

        return authenticateGuestUser(
            userId = savedUser.id,
            request = request,
            response = response,
        )
    }

    @Transactional
    override fun createUser(
        email: String,
        password: String?,
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
        roleNames: Set<String>,
    ): UserDto {
        // Create user via unified service
        val userDto =
            userService.createUser(
                CreateUserRequest(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    password = password?.let { passwordEncoder.encode(it) },
                ),
            )

        // Assign roles using the unified service
        if (roleNames.isNotEmpty()) {
            userService.setUserRoles(userDto.id, roleNames)
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

        return LoginResponse(
            user = userDto,
            sessionId = session.id,
            roles = userRoles.toList(),
        )
    }
}
