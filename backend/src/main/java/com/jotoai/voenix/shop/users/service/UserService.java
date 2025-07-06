package com.jotoai.voenix.shop.users.service;

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException;
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException;
import com.jotoai.voenix.shop.users.dto.CreateUserRequest;
import com.jotoai.voenix.shop.users.dto.UpdateUserRequest;
import com.jotoai.voenix.shop.users.dto.UserDto;
import com.jotoai.voenix.shop.users.entity.User;
import com.jotoai.voenix.shop.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    public UserDto getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User", "email", request.email());
        }
        
        var user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .password(request.password())
                .build();
        
        var savedUser = userRepository.save(user);
        return toDto(savedUser);
    }
    
    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ResourceAlreadyExistsException("User", "email", request.email());
            }
            user.setEmail(request.email());
        }
        
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        
        if (request.password() != null) {
            user.setPassword(request.password());
        }
        
        if (request.oneTimePassword() != null) {
            user.setOneTimePassword(request.oneTimePassword());
        }
        
        var updatedUser = userRepository.save(user);
        return toDto(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }
    
    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}