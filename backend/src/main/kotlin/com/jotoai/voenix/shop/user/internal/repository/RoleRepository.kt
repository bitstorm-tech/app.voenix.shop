package com.jotoai.voenix.shop.user.internal.repository

import com.jotoai.voenix.shop.user.internal.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * Repository for Role entity operations.
 */
@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    /**
     * Finds a role by its name.
     */
    fun findByName(name: String): Optional<Role>

    /**
     * Checks if a role exists by name.
     */
    fun existsByName(name: String): Boolean

    /**
     * Finds roles by name in a collection.
     */
    fun findByNameIn(names: Collection<String>): List<Role>

    /**
     * Finds all role names.
     */
    @Query("SELECT r.name FROM Role r")
    fun findAllRoleNames(): List<String>
}
