package com.jotoai.voenix.shop.users.dto;

import java.time.OffsetDateTime;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    // Builder pattern for easier construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserDto build() {
            return new UserDto(id, email, firstName, lastName, phoneNumber, createdAt, updatedAt);
        }
    }
}