package com.jotoai.voenix.shop.api.user

import com.jotoai.voenix.shop.auth.dto.SessionInfo
import com.jotoai.voenix.shop.auth.service.AuthService
import com.jotoai.voenix.shop.domain.users.dto.UpdateUserRequest
import com.jotoai.voenix.shop.domain.users.dto.UserDto
import com.jotoai.voenix.shop.domain.users.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
class UserAuthController(
    private val authService: AuthService,
    private val userService: UserService,
) {
    @GetMapping("/profile")
    fun getCurrentUserProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): UserDto = userService.getUserByEmail(userDetails.username)

    @PutMapping("/profile")
    fun updateCurrentUserProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody updateUserRequest: UpdateUserRequest,
    ): UserDto {
        val currentUser = userService.getUserByEmail(userDetails.username)
        return userService.updateUser(currentUser.id, updateUserRequest)
    }

    @GetMapping("/session")
    fun getSessionInfo(): SessionInfo = authService.getCurrentSession()

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest) {
        authService.logout(request)
    }

    @DeleteMapping("/account")
    fun deleteCurrentUserAccount(
        @AuthenticationPrincipal userDetails: UserDetails,
        request: HttpServletRequest,
    ) {
        val currentUser = userService.getUserByEmail(userDetails.username)
        userService.deleteUser(currentUser.id)
        authService.logout(request)
    }
}
