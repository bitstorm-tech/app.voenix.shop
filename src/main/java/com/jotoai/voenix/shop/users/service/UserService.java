package com.jotoai.voenix.shop.users.service;

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException;
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException;
import com.jotoai.voenix.shop.users.dto.CreateUserRequest;
import com.jotoai.voenix.shop.users.dto.UpdateUserRequest;
import com.jotoai.voenix.shop.users.dto.UserDto;
import com.jotoai.voenix.shop.users.entity.User;
import com.jotoai.voenix.shop.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
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
    
    public UserDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .build();
        
        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }
    
    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        User updatedUser = userRepository.save(user);
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
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}