package com.jotoai.voenix.shop.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class UpdateUserRequest {
    
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
    
    @Size(max = 255, message = "One time password must not exceed 255 characters")
    private String oneTimePassword;

    // Constructors
    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String email, String firstName, String lastName, String phoneNumber, 
                           String password, String oneTimePassword) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.oneTimePassword = oneTimePassword;
    }

    // Builder pattern
    public static UpdateUserRequestBuilder builder() {
        return new UpdateUserRequestBuilder();
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

    public String getOneTimePassword() {
        return oneTimePassword;
    }

    public void setOneTimePassword(String oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateUserRequest that = (UpdateUserRequest) o;
        return Objects.equals(email, that.email) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(password, that.password) &&
                Objects.equals(oneTimePassword, that.oneTimePassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, firstName, lastName, phoneNumber, password, oneTimePassword);
    }

    // toString
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

    // Builder class
    public static class UpdateUserRequestBuilder {
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String password;
        private String oneTimePassword;

        UpdateUserRequestBuilder() {
        }

        public UpdateUserRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UpdateUserRequestBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UpdateUserRequestBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UpdateUserRequestBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UpdateUserRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UpdateUserRequestBuilder oneTimePassword(String oneTimePassword) {
            this.oneTimePassword = oneTimePassword;
            return this;
        }

        public UpdateUserRequest build() {
            return new UpdateUserRequest(email, firstName, lastName, phoneNumber, password, oneTimePassword);
        }
    }
}