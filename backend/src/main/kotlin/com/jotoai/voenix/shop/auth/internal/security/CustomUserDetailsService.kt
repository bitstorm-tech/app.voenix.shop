package com.jotoai.voenix.shop.auth.internal.security

import com.jotoai.voenix.shop.auth.internal.security.CustomUserDetails
import com.jotoai.voenix.shop.user.api.UserAuthenticationService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userAuthenticationService: UserAuthenticationService,
) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val authDto =
            userAuthenticationService.loadUserByEmail(username)
                ?: throw UsernameNotFoundException("User not found with email: $username")

        return CustomUserDetails(
            id = authDto.id,
            email = authDto.email,
            passwordHash = authDto.passwordHash,
            userRoles = authDto.roles,
        )
    }
}
