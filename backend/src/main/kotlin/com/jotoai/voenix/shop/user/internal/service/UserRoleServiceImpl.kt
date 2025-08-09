package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.user.api.UserRoleManagementService
import com.jotoai.voenix.shop.user.api.exceptions.createUserNotFoundException
import com.jotoai.voenix.shop.user.internal.repository.RoleRepository
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of UserRoleManagementService for managing user roles.
 * This service handles all role-related operations for users.
 */
@Service
@Transactional(readOnly = true)
class UserRoleServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
) : UserRoleManagementService {
    @Transactional
    override fun assignRoles(
        userId: Long,
        roleNames: Set<String>,
    ) {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        val rolesToAssign = roleRepository.findByNameIn(roleNames)

        // Verify all roles exist
        val foundRoleNames = rolesToAssign.map { it.name }.toSet()
        val missingRoles = roleNames - foundRoleNames
        if (missingRoles.isNotEmpty()) {
            throw ResourceNotFoundException("Role", "names", missingRoles.toString())
        }

        // Add new roles to existing ones
        user.roles.addAll(rolesToAssign)

        val savedUser = userRepository.save(user)
    }

    @Transactional
    override fun removeRoles(
        userId: Long,
        roleNames: Set<String>,
    ) {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        // Remove roles by name
        user.roles.removeAll { it.name in roleNames }
    }

    override fun getUserRoles(userId: Long): Set<String> {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        return user.roles.map { it.name }.toSet()
    }

    @Transactional
    override fun setUserRoles(
        userId: Long,
        roleNames: Set<String>,
    ) {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        val rolesToSet =
            if (roleNames.isNotEmpty()) {
                val roles = roleRepository.findByNameIn(roleNames)

                // Verify all roles exist
                val foundRoleNames = roles.map { it.name }.toSet()
                val missingRoles = roleNames - foundRoleNames
                if (missingRoles.isNotEmpty()) {
                    throw ResourceNotFoundException("Role", "names", missingRoles.toString())
                }

                roles.toSet()
            } else {
                emptySet()
            }

        // Clear existing roles and add new ones instead of replacing the collection
        // This avoids issues with Hibernate's managed collections
        user.roles.clear()
        user.roles.addAll(rolesToSet)
    }

    override fun userHasRole(
        userId: Long,
        roleName: String,
    ): Boolean {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        return user.roles.any { it.name == roleName }
    }

    override fun getAllRoleNames(): Set<String> = roleRepository.findAllRoleNames().toSet()
}
