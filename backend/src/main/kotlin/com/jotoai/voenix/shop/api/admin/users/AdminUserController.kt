package com.jotoai.voenix.shop.api.admin.users

import com.jotoai.voenix.shop.user.api.UserFacade
import com.jotoai.voenix.shop.user.api.UserQueryService
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
    private val userFacade: UserFacade,
    private val userQueryService: UserQueryService,
) {
    @GetMapping
    fun getAllUsers(): List<UserDto> = userQueryService.getAllUsers()

    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: Long,
    ): UserDto = userQueryService.getUserById(id)

    @PostMapping
    fun createUser(
        @Valid @RequestBody createUserRequest: CreateUserRequest,
    ): UserDto = userFacade.createUser(createUserRequest)

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody updateUserRequest: UpdateUserRequest,
    ): UserDto = userFacade.updateUser(id, updateUserRequest)

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Long,
    ) {
        userFacade.deleteUser(id)
    }
}
