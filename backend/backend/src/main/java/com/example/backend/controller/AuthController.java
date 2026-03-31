package com.example.backend.controller;

import com.example.backend.Entities.User;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.enums.Role;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Signup
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.EMPLOYEE);

        userRepository.save(user);

        return new ApiResponse<>("success", "User registered successfully", null);
    }


    // Login
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody User user) {

        User existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(existingUser.getEmail());

        return new ApiResponse<>(
                "success",
                "Login successful",
                Map.of("token", token)
        );
    }
    }
