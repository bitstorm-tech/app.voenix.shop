package com.jotoai.voenix.shop.auth.internal.service

import com.jotoai.voenix.shop.auth.api.AuthRegistrationService
import com.jotoai.voenix.shop.user.api.dto.UserDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthRegistrationServiceImpl(
    private val userRegistrationService: UserRegistrationService,
) : AuthRegistrationService {
    @Transactional
    override fun createUser(
        email: String,
        password: String?,
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
        roleNames: Set<String>,
    ): UserDto =
        userRegistrationService.createUser(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            roleNames = roleNames,
        )

    @Transactional
    override fun updateUser(
        userId: Long,
        firstName: String?,
        lastName: String?,
        phoneNumber: String?,
    ): UserDto =
        userRegistrationService.updateUser(
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
        )
}
