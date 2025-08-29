package com.jotoai.voenix.shop.user

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Architecture tests for the User module.
 * These tests verify that module boundaries are properly maintained using simple checks.
 */
class UserModuleArchitectureTest {
    @Test
    fun `verify user module structure exists`() {
        // Simple test to verify the user module structure is in place
        val userApiPackage = "com.jotoai.voenix.shop.user.api"
        val userInternalPackage = "com.jotoai.voenix.shop.user.internal"

        // The unified UserService interface should exist in the API package
        assertClassExists("$userApiPackage.UserService")

        // The unified UserServiceImpl should exist in the internal package
        assertClassExists("$userInternalPackage.service.UserServiceImpl")

        println("✓ User module structure verified")
    }

    @Test
    fun `verify user API classes are interfaces`() {
        // Verify that the API classes are properly designed as interfaces
        val userService = Class.forName("com.jotoai.voenix.shop.user.api.UserService")
        assertTrue(userService.isInterface, "UserService should be an interface")

        println("✓ User API interfaces verified")
    }

    @Test
    fun `verify user DTOs exist`() {
        // Verify that the essential DTOs exist
        assertClassExists("com.jotoai.voenix.shop.user.api.dto.UserDto")
        assertClassExists("com.jotoai.voenix.shop.user.api.dto.CreateUserRequest")
        assertClassExists("com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest")
        assertClassExists("com.jotoai.voenix.shop.user.api.dto.UserAuthenticationDto")
        assertClassExists("com.jotoai.voenix.shop.user.api.dto.BulkCreateUsersRequest")
        assertClassExists("com.jotoai.voenix.shop.user.api.dto.BulkOperationResult")
        assertClassExists("com.jotoai.voenix.shop.user.api.dto.UserSearchCriteria")
        assertClassExists("com.jotoai.voenix.shop.user.api.PasswordValidationResult")

        println("✓ User DTOs verified")
    }

    @Test
    fun `verify user entity and repositories exist`() {
        // Verify internal implementation exists
        assertClassExists("com.jotoai.voenix.shop.user.internal.entity.User")
        assertClassExists("com.jotoai.voenix.shop.user.internal.entity.Role")
        assertClassExists("com.jotoai.voenix.shop.user.internal.repository.UserRepository")
        assertClassExists("com.jotoai.voenix.shop.user.internal.repository.RoleRepository")
        assertClassExists("com.jotoai.voenix.shop.user.internal.repository.UserSpecifications")

        println("✓ User internal components verified")
    }

    private fun assertClassExists(className: String) {
        try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw AssertionError("Expected class $className to exist, but it was not found", e)
        }
    }
}
