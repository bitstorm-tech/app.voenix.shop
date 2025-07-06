package com.jotoai.voenix.shop.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 255, message = "First name must not exceed 255 characters")
    private String firstName;
    
    @Size(max = 255, message = "Last name must not exceed 255 characters")
    private String lastName;
    
    @Size(max = 255, message = "Phone number must not exceed 255 characters")
    private String phoneNumber;
    
    @Size(max = 255, message = "Password must not exceed 255 characters")
    private String password;

    // Constructors
    public CreateUserRequest() {
    }

    public CreateUserRequest(String email, String firstName, String lastName, String phoneNumber, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // Builder pattern
    public static CreateUserRequestBuilder builder() {
        return new CreateUserRequestBuilder();
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateUserRequest that = (CreateUserRequest) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, firstName, lastName, phoneNumber, password);
    }

    // toString
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

    // Builder class
    public static class CreateUserRequestBuilder {
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String password;

        CreateUserRequestBuilder() {
        }

        public CreateUserRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CreateUserRequestBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public CreateUserRequestBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public CreateUserRequestBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public CreateUserRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public CreateUserRequest build() {
            return new CreateUserRequest(email, firstName, lastName, phoneNumber, password);
        }
    }
}