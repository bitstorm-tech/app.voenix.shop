package com.jotoai.voenix.shop.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,
        
        @Size(max = 255, message = "First name must not exceed 255 characters")
        String firstName,
        
        @Size(max = 255, message = "Last name must not exceed 255 characters")
        String lastName,
        
        @Size(max = 255, message = "Phone number must not exceed 255 characters")
        String phoneNumber,
        
        @Size(max = 255, message = "Password must not exceed 255 characters")
        String password
) {
    // Override toString to hide password
    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + (password != null ? "[PROTECTED]" : null) + '\'' +
                '}';
    }
    
    // Builder pattern for easier construction (especially useful in tests)
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String password;
        
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
        
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        
        public CreateUserRequest build() {
            return new CreateUserRequest(email, firstName, lastName, phoneNumber, password);
        }
    }
}