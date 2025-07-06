package com.jotoai.voenix.shop.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class UpdateUserRequest {
    
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    // Constructors
    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Builder pattern
    public static UpdateUserRequestBuilder builder() {
        return new UpdateUserRequestBuilder();
    }

    // Getters and Setters
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

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateUserRequest that = (UpdateUserRequest) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    // toString
    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // Builder class
    public static class UpdateUserRequestBuilder {
        private String username;
        private String email;

        UpdateUserRequestBuilder() {
        }

        public UpdateUserRequestBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UpdateUserRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UpdateUserRequest build() {
            return new UpdateUserRequest(username, email);
        }
    }
}