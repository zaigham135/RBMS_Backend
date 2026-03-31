package com.example.backend.service.impl;

import com.example.backend.Entities.User;
import com.example.backend.enums.Role;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        throw new UnauthorizedException("Invalid authentication");
    }

    @Override
    public void updateUserRole(Long userId, String role) {

        User currentUser = getCurrentUser();

        if (!"ADMIN".equals(currentUser.getRole().name())) {
            throw new UnauthorizedException("Only admin can update roles");
        }

        if (role == null || role.isBlank()) {
            throw new BadRequestException("Role cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            user.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + role + ". Allowed: ADMIN, MANAGER, EMPLOYEE");
        }

        userRepository.save(user);
    }
}