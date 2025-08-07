package com.jotoai.voenix.shop.user.internal.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserEqualsHashCodeTest {
    @Test
    fun `equals should return true for users with same email`() {
        // Given
        val user1 = User(id = 1L, email = "test@example.com")
        val user2 = User(id = 2L, email = "test@example.com")

        // When & Then
        assertThat(user1).isEqualTo(user2)
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode())
    }

    @Test
    fun `equals should return false for users with different emails`() {
        // Given
        val user1 = User(id = 1L, email = "test1@example.com")
        val user2 = User(id = 1L, email = "test2@example.com")

        // When & Then
        assertThat(user1).isNotEqualTo(user2)
    }

    @Test
    fun `equals should return true for same user instance`() {
        // Given
        val user = User(id = 1L, email = "test@example.com")

        // When & Then
        assertThat(user).isEqualTo(user)
    }

    @Test
    fun `equals should return false when comparing with null`() {
        // Given
        val user = User(id = 1L, email = "test@example.com")

        // When & Then
        assertThat(user).isNotEqualTo(null)
    }

    @Test
    fun `equals should return false when comparing with different type`() {
        // Given
        val user = User(id = 1L, email = "test@example.com")
        val role = Role(id = 1L, name = "ADMIN")

        // When & Then
        assertThat(user).isNotEqualTo(role)
    }
}
