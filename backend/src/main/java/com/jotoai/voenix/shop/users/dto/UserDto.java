package com.jotoai.voenix.shop.users.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UserDto() {
    }

    public UserDto(Long id, String username, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder pattern
    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(id, userDto.id) &&
                Objects.equals(username, userDto.username) &&
                Objects.equals(email, userDto.email) &&
                Objects.equals(createdAt, userDto.createdAt) &&
                Objects.equals(updatedAt, userDto.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, createdAt, updatedAt);
    }

    // toString
    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Builder class
    public static class UserDtoBuilder {
        private Long id;
        private String username;
        private String email;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        UserDtoBuilder() {
        }

        public UserDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserDtoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserDtoBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserDto build() {
            return new UserDto(id, username, email, createdAt, updatedAt);
        }
    }
}