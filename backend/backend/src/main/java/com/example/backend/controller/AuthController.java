package com.example.backend.controller;

import com.example.backend.Entities.User;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.enums.Role;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.ImageKitService;
import com.example.backend.service.OtpService;
import com.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private ImageKitService imageKitService;
    @Autowired private EmailService emailService;
    @Autowired private OtpService otpService;

    // ── Signup ────────────────────────────────────────────────────────────────
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

    // ── Login ─────────────────────────────────────────────────────────────────
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
        return new ApiResponse<>("success", "Login successful", Map.of(
                "token", token,
                "role", existingUser.getRole().name(),
                "name", existingUser.getName(),
                "email", existingUser.getEmail(),
                "userId", String.valueOf(existingUser.getId()),
                "profilePhoto", existingUser.getProfilePhoto() != null ? existingUser.getProfilePhoto() : ""
        ));
    }

    // ── Forgot Password — Step 1: Send OTP ───────────────────────────────────
    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<Void> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) throw new BadRequestException("Email is required");

        // Verify the email exists
        userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        String otp = otpService.generateOtp(email.trim().toLowerCase());

        String subject = "🔐 TaskMan — Password Reset OTP";
        String emailBody = String.format("""
                Hello,

                You requested a password reset for your TaskMan account.

                Your OTP code is:

                    %s

                This code expires in 10 minutes. Do not share it with anyone.

                If you did not request this, please ignore this email.

                — TaskMan Security
                """, otp);

        emailService.sendTaskUpdateEmail(email.trim().toLowerCase(), subject, emailBody);

        return new ApiResponse<>("success", "OTP sent to your registered email address", null);
    }

    // ── Forgot Password — Step 2: Verify OTP ─────────────────────────────────
    @PostMapping("/forgot-password/verify-otp")
    public ApiResponse<Void> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");
        if (email == null || otp == null) throw new BadRequestException("Email and OTP are required");

        if (!otpService.validateOtp(email.trim().toLowerCase(), otp.trim())) {
            throw new BadRequestException("Invalid or expired OTP. Please request a new one.");
        }
        return new ApiResponse<>("success", "OTP verified successfully", null);
    }

    // ── Forgot Password — Step 3: Reset Password ──────────────────────────────
    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody Map<String, String> body) {
        String email       = body.get("email");
        String otp         = body.get("otp");
        String newPassword = body.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            throw new BadRequestException("Email, OTP, and new password are required");
        }
        if (newPassword.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        // Re-validate OTP before resetting
        if (!otpService.validateOtp(email.trim().toLowerCase(), otp.trim())) {
            throw new BadRequestException("Invalid or expired OTP. Please start over.");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Consume the OTP so it can't be reused
        otpService.invalidateOtp(email.trim().toLowerCase());

        return new ApiResponse<>("success", "Password reset successfully. You can now log in.", null);
    }

    // ── Upload profile photo ──────────────────────────────────────────────────
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
