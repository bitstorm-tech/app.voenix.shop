package com.jotoai.voenix.shop.auth.internal.security

import com.jotoai.voenix.shop.user.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userService: UserService,
) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val userDto =
            userService.getUserByEmail(username, includeAuth = true)
                ?: throw UsernameNotFoundException("User not found with email: $username")

        return CustomUserDetails(
            id = userDto.id,
            email = userDto.email,
            passwordHash = userDto.passwordHash,
            userRoles = userDto.roles,
        )
    }
}
