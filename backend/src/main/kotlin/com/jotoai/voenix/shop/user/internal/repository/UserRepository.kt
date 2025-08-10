package com.jotoai.voenix.shop.user.internal.repository

import com.jotoai.voenix.shop.user.internal.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository :
    JpaRepository<User, Long>,
    JpaSpecificationExecutor<User> {
    // Basic queries with soft delete support
    fun findByEmail(email: String): Optional<User>

    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    fun findActiveByEmail(
        @Param("email") email: String,
    ): Optional<User>

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    fun findActiveById(
        @Param("id") id: Long,
    ): Optional<User>

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id AND u.deletedAt IS NULL")
    fun findActiveByIdWithRoles(
        @Param("id") id: Long,
    ): Optional<User>

    // Active user queries
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    fun findAllActive(): List<User>

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    fun findAllActive(pageable: Pageable): Page<User>

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    fun countActive(): Long

    // Batch queries
    @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.deletedAt IS NULL")
    fun findActiveByIds(
        @Param("ids") ids: List<Long>,
    ): List<User>

    // Role-based queries
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames AND u.deletedAt IS NULL")
    fun findActiveByRoleNames(
        @Param("roleNames") roleNames: Set<String>,
    ): List<User>

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames AND u.deletedAt IS NULL")
    fun findActiveByRoleNames(
        @Param("roleNames") roleNames: Set<String>,
        pageable: Pageable,
    ): Page<User>

    // Existence checks with active constraint
    @Query(
        "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email AND u.deletedAt IS NULL"
    )
    fun existsActiveByEmail(
        @Param("email") email: String,
    ): Boolean

    @Query(
        "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email AND u.id != :excludeId AND u.deletedAt IS NULL",
    )
    fun existsActiveByEmailAndIdNot(
        @Param("email") email: String,
        @Param("excludeId") excludeId: Long,
    ): Boolean
}
