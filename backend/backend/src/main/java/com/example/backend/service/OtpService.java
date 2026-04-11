package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_EXPIRY_SECONDS = 600; // 10 minutes
    private static final int OTP_LENGTH = 6;

    private record OtpEntry(String otp, Instant expiresAt) {}

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    /** Generate and store a 6-digit OTP for the given email. Returns the OTP. */
    public String generateOtp(String email) {
        String otp = String.format("%0" + OTP_LENGTH + "d", random.nextInt((int) Math.pow(10, OTP_LENGTH)));
        store.put(email.toLowerCase(), new OtpEntry(otp, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS)));
        return otp;
    }

    /** Validate the OTP for the given email. Returns true if valid and not expired. */
    public boolean validateOtp(String email, String otp) {
        OtpEntry entry = store.get(email.toLowerCase());
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(email.toLowerCase());
            return false;
        }
        return entry.otp().equals(otp);
    }

    /** Invalidate (consume) the OTP after successful password reset. */
    public void invalidateOtp(String email) {
        store.remove(email.toLowerCase());
    }
}
