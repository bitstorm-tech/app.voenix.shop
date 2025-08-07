package com.jotoai.voenix.shop.auth.service

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
        val userDetails = userAuthenticationService.loadUserByEmail(username)
        return userDetails ?: throw UsernameNotFoundException("User not found with email: $username")
    }
}
