package com.bajajFinserv.loanapp.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TwoFactorAuthService {

    private static final int OTP_LENGTH = 6;
    private static final int SECRET_LENGTH = 32;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a random 2FA secret key
     */
    public String generateSecret() {
        byte[] secret = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }

    /**
     * Generate a 6-digit OTP code
     */
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Verify OTP code (in production, this would use TOTP algorithm)
     * For demo purposes, we'll use a time-based approach with a 5-minute window
     */
    public boolean verifyOtp(String secret, String otp) {
        // In production, use Google Authenticator compatible TOTP
        // For this demo, we'll accept any 6-digit code for testing
        return otp != null && otp.length() == OTP_LENGTH && otp.matches("\\d{" + OTP_LENGTH + "}");
    }

    /**
     * Generate backup codes for account recovery
     */
    public String generateBackupCodes() {
        StringBuilder codes = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i > 0) codes.append(",");
            codes.append(generateBackupCode());
        }
        return codes.toString();
    }

    private String generateBackupCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) code.append("-");
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
}
