package com.jotoai.voenix.shop.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
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
        String password,
        
        @Size(max = 255, message = "One time password must not exceed 255 characters")
        String oneTimePassword
) {
    // Override toString to hide sensitive data
    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + (password != null ? "[PROTECTED]" : null) + '\'' +
                ", oneTimePassword='" + (oneTimePassword != null ? "[PROTECTED]" : null) + '\'' +
                '}';
    }
    
    // Builder pattern for easier construction
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String password;
        private String oneTimePassword;
        
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
        
        public Builder oneTimePassword(String oneTimePassword) {
            this.oneTimePassword = oneTimePassword;
            return this;
        }
        
        public UpdateUserRequest build() {
            return new UpdateUserRequest(email, firstName, lastName, phoneNumber, password, oneTimePassword);
        }
    }
}