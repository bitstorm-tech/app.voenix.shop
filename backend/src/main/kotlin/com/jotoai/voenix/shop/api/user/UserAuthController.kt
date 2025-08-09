package com.jotoai.voenix.shop.api.user

import com.jotoai.voenix.shop.auth.api.AuthFacade
import com.jotoai.voenix.shop.auth.api.AuthQueryService
import com.jotoai.voenix.shop.auth.api.dto.SessionInfo
import com.jotoai.voenix.shop.user.api.UserFacade
import com.jotoai.voenix.shop.user.api.UserQueryService
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto
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
    private val authFacade: AuthFacade,
    private val authQueryService: AuthQueryService,
    private val userFacade: UserFacade,
    private val userQueryService: UserQueryService,
) {
    @GetMapping("/profile")
    fun getCurrentUserProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): UserDto = userQueryService.getUserByEmail(userDetails.username)

    @PutMapping("/profile")
    fun updateCurrentUserProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody updateUserRequest: UpdateUserRequest,
    ): UserDto {
        val currentUser = userQueryService.getUserByEmail(userDetails.username)
        return userFacade.updateUser(currentUser.id, updateUserRequest)
    }

    @GetMapping("/session")
    fun getSessionInfo(): SessionInfo = authQueryService.getCurrentSession()

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest) {
        authFacade.logout(request)
    }

    @DeleteMapping("/account")
    fun deleteCurrentUserAccount(
        @AuthenticationPrincipal userDetails: UserDetails,
        request: HttpServletRequest,
    ) {
        val currentUser = userQueryService.getUserByEmail(userDetails.username)
        userFacade.deleteUser(currentUser.id)
        authFacade.logout(request)
    }
}
