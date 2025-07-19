package com.jotoai.voenix.shop.auth.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

data class CustomUserDetails(
    val id: Long,
    private val email: String,
    private val passwordHash: String?,
    private val userRoles: Set<String>,
) : UserDetails, Serializable {

    override fun getAuthorities(): Collection<GrantedAuthority> = userRoles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun getPassword(): String? = passwordHash

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
