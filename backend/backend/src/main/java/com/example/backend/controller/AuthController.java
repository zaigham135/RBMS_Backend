package com.example.backend.controller;

import com.example.backend.Entities.User;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.enums.Role;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ImageKitService;
import com.example.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private ImageKitService imageKitService;

    // Signup
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
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

        if ("INACTIVE".equals(existingUser.getStatus())) {
            throw new BadRequestException("Your account has been deactivated. Contact admin.");
        }

        String token = jwtUtil.generateToken(existingUser.getEmail());

        return new ApiResponse<>(
                "success",
                "Login successful",
                Map.of(
                        "token", token,
                        "role", existingUser.getRole().name(),
                        "name", existingUser.getName(),
                        "email", existingUser.getEmail(),
                        "userId", String.valueOf(existingUser.getId()),
                        "profilePhoto", existingUser.getProfilePhoto() != null ? existingUser.getProfilePhoto() : ""
                )
        );
    }

    // Upload profile photo
    @PostMapping("/upload-photo")
    public ApiResponse<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (file.isEmpty()) throw new BadRequestException("File is required");

        String fileName = "user_" + user.getId() + "_" + file.getOriginalFilename();
        String photoUrl = imageKitService.uploadProfilePhoto(file, fileName);

        user.setProfilePhoto(photoUrl);
        userRepository.save(user);

        return new ApiResponse<>("success", "Profile photo uploaded successfully",
                Map.of("profilePhoto", photoUrl));
    }
    }
