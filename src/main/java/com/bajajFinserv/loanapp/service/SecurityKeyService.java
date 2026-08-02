package com.bajajFinserv.loanapp.service;

import com.bajajFinserv.loanapp.model.SecurityKey;
import com.bajajFinserv.loanapp.repository.SecurityKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityKeyService {

    @Autowired
    private SecurityKeyRepository securityKeyRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = UPPER + DIGITS;

    /**
     * Generate a unique security key code
     */
    public String generateKeyCode() {
        String keyCode;
        do {
            keyCode = generateRandomString(8);
        } while (securityKeyRepository.existsByKeyCode(keyCode));
        return keyCode;
    }

    /**
     * Generate a random string of specified length
     */
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(secureRandom.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Create a new security key
     */
    public SecurityKey createSecurityKey(String role, String createdBy, int maxUses, String notes) {
        SecurityKey securityKey = new SecurityKey();
        securityKey.setKeyCode(generateKeyCode());
        securityKey.setRole(role);
        securityKey.setCreatedBy(createdBy);
        securityKey.setMaxUses(maxUses);
        securityKey.setCurrentUses(0);
        securityKey.setActive(true);
        securityKey.setUsed(false);
        securityKey.setCreatedAt(LocalDateTime.now());
        securityKey.setExpiresAt(LocalDateTime.now().plusMonths(6)); // Keys expire in 6 months
        securityKey.setNotes(notes);

        return securityKeyRepository.save(securityKey);
    }

    /**
     * Validate a security key for registration
     */
    public boolean validateSecurityKey(String keyCode, String role) {
        Optional<SecurityKey> keyOpt = securityKeyRepository.findByKeyCode(keyCode);

        if (keyOpt.isEmpty()) {
            return false;
        }

        SecurityKey securityKey = keyOpt.get();

        // Check if key is active
        if (!securityKey.isActive()) {
            return false;
        }

        // Check if key matches the required role
        if (!securityKey.getRole().equalsIgnoreCase(role)) {
            return false;
        }

        // Check if key has expired
        if (securityKey.getExpiresAt() != null && securityKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Check if key has reached max uses
        if (securityKey.getCurrentUses() >= securityKey.getMaxUses()) {
            return false;
        }

        return true;
    }

    /**
     * Mark a security key as used
     */
    public void markKeyAsUsed(String keyCode, String assignedTo) {
        Optional<SecurityKey> keyOpt = securityKeyRepository.findByKeyCode(keyCode);

        if (keyOpt.isPresent()) {
            SecurityKey securityKey = keyOpt.get();
            securityKey.setCurrentUses(securityKey.getCurrentUses() + 1);
            securityKey.setAssignedTo(assignedTo);
            securityKey.setUsedAt(LocalDateTime.now());

            // Mark as used if max uses reached
            if (securityKey.getCurrentUses() >= securityKey.getMaxUses()) {
                securityKey.setUsed(true);
            }

            securityKeyRepository.save(securityKey);
        }
    }

    /**
     * Get all security keys
     */
    public List<SecurityKey> getAllSecurityKeys() {
        return securityKeyRepository.findAll();
    }

    /**
     * Get security keys by role
     */
    public List<SecurityKey> getSecurityKeysByRole(String role) {
        return securityKeyRepository.findByRole(role);
    }

    /**
     * Get active security keys
     */
    public List<SecurityKey> getActiveSecurityKeys() {
        return securityKeyRepository.findByActiveTrue();
    }

    /**
     * Get available (active and not used) security keys
     */
    public List<SecurityKey> getAvailableSecurityKeys() {
        return securityKeyRepository.findByActiveTrueAndUsedFalse();
    }

    /**
     * Deactivate a security key
     */
    public void deactivateSecurityKey(Long id) {
        Optional<SecurityKey> keyOpt = securityKeyRepository.findById(id);
        if (keyOpt.isPresent()) {
            SecurityKey securityKey = keyOpt.get();
            securityKey.setActive(false);
            securityKeyRepository.save(securityKey);
        }
    }

    /**
     * Delete a security key
     */
    public void deleteSecurityKey(Long id) {
        securityKeyRepository.deleteById(id);
    }

    /**
     * Get security key by ID
     */
    public Optional<SecurityKey> getSecurityKeyById(Long id) {
        return securityKeyRepository.findById(id);
    }
}
