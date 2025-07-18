package com.jotoai.voenix.shop.api.admin.users

import com.jotoai.voenix.shop.domain.users.dto.CreateUserRequest
import com.jotoai.voenix.shop.domain.users.dto.UpdateUserRequest
import com.jotoai.voenix.shop.domain.users.dto.UserDto
import com.jotoai.voenix.shop.domain.users.service.UserService
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
    private val userService: UserService,
) {
    @GetMapping
    fun getAllUsers(): List<UserDto> = userService.getAllUsers()

    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: Long,
    ): UserDto = userService.getUserById(id)

    @PostMapping
    fun createUser(
        @Valid @RequestBody createUserRequest: CreateUserRequest,
    ): UserDto = userService.createUser(createUserRequest)

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody updateUserRequest: UpdateUserRequest,
    ): UserDto = userService.updateUser(id, updateUserRequest)

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Long,
    ) {
        userService.deleteUser(id)
    }
}
