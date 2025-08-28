package com.jotoai.voenix.shop.user.internal.repository

import com.jotoai.voenix.shop.user.internal.entity.User
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
    fun findByEmail(email: String): Optional<User>

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    fun findActiveByEmail(
        @Param("email") email: String,
    ): Optional<User>

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    fun findActiveById(
        @Param("id") id: Long,
    ): Optional<User>

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    fun countActive(): Long

    @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.deletedAt IS NULL")
    fun findActiveByIds(
        @Param("ids") ids: List<Long>,
    ): List<User>

    @Query(
        "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email AND u.deletedAt IS NULL",
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
