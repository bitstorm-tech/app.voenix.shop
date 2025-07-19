package com.jotoai.voenix.shop.auth.service

import com.jotoai.voenix.shop.auth.dto.CustomUserDetails
import com.jotoai.voenix.shop.domain.users.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository
                .findByEmail(username)
                .orElseThrow { UsernameNotFoundException("User not found with email: $username") }

        return CustomUserDetails(
            id = user.id!!,
            email = user.email,
            passwordHash = user.password,
            userRoles = user.roles.map { it.name }.toSet(),
        )
    }
}
