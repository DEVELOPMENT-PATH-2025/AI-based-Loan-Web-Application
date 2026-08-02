package com.bajajFinserv.loanapp.controller;

import com.bajajFinserv.loanapp.model.LoanUser;
import com.bajajFinserv.loanapp.repository.UserRepository;
import com.bajajFinserv.loanapp.security.JwtUtils;
import com.bajajFinserv.loanapp.service.SecurityKeyService;
import com.bajajFinserv.loanapp.service.TwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JwtUtils jwtUtils;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private SecurityKeyService securityKeyService;

    // ==========================================
    // 1. Thymeleaf UI Endpoints (HTML Pages)
    // ==========================================

    @GetMapping("/")
    public String showLandingPage() {
        return "index"; // Loads src/main/resources/templates/index.html
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login"; // Loads src/main/resources/templates/auth/login.html
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register"; // Loads src/main/resources/templates/auth/register.html
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        Optional<LoanUser> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email not found. Please try again.");
            return "redirect:/forgot-password?error";
        }
        
        LoanUser user = userOpt.get();
        
        // Generate reset token (in production, this would be a secure random token)
        String resetToken = java.util.UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        
        // In production, send email with reset link
        // For now, we'll log it and redirect to a reset page with the token
        System.out.println("Password reset token for " + email + ": " + resetToken);
        
        redirectAttributes.addFlashAttribute("resetToken", resetToken);
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(org.springframework.ui.Model model,
                                        @ModelAttribute("resetToken") String resetToken,
                                        @ModelAttribute("email") String email) {
        model.addAttribute("resetToken", resetToken);
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String email,
                                       @RequestParam String resetToken,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        Optional<LoanUser> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return "redirect:/forgot-password?error";
        }
        
        LoanUser user = userOpt.get();
        
        // Validate token
        if (user.getResetToken() == null || !user.getResetToken().equals(resetToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired reset token");
            return "redirect:/forgot-password?error";
        }
        
        // Check token expiry
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Reset token has expired");
            return "redirect:/forgot-password?error";
        }
        
        // Validate new password
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/reset-password?error";
        }
        
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
            return "redirect:/reset-password?error";
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        
        return "redirect:/login?success=password_reset";
    }

    // ==========================================
    // 2FA Endpoints
    // ==========================================

    @GetMapping("/2fa/setup")
    public String showTwoFactorSetupPage(org.springframework.ui.Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String email = authentication.getName();
        Optional<LoanUser> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        LoanUser user = userOpt.get();
        model.addAttribute("twoFactorEnabled", user.isTwoFactorEnabled());
        model.addAttribute("email", email);
        
        return "auth/2fa-setup";
    }

    @PostMapping("/2fa/enable")
    @ResponseBody
    public ResponseEntity<?> enableTwoFactor(Authentication authentication) {
        if (authentication == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(401).body(error);
        }
        
        String email = authentication.getName();
        Optional<LoanUser> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.status(404).body(error);
        }
        
        LoanUser user = userOpt.get();
        String secret = twoFactorAuthService.generateSecret();
        String backupCodes = twoFactorAuthService.generateBackupCodes();
        
        user.setTwoFactorSecret(secret);
        user.setBackupCodes(backupCodes);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "2FA enabled successfully");
        response.put("secret", secret);
        response.put("backupCodes", backupCodes.split(","));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/disable")
    @ResponseBody
    public ResponseEntity<?> disableTwoFactor(Authentication authentication) {
        if (authentication == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(401).body(error);
        }
        
        String email = authentication.getName();
        Optional<LoanUser> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.status(404).body(error);
        }
        
        LoanUser user = userOpt.get();
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setBackupCodes(null);
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA disabled successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/verify")
    @ResponseBody
    public ResponseEntity<?> verifyTwoFactor(@RequestBody Map<String, String> request, Authentication authentication) {
        if (authentication == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(401).body(error);
        }
        
        String email = authentication.getName();
        Optional<LoanUser> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.status(404).body(error);
        }
        
        LoanUser user = userOpt.get();
        String otp = request.get("otp");
        
        if (!twoFactorAuthService.verifyOtp(user.getTwoFactorSecret(), otp)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid OTP code");
            return ResponseEntity.status(400).body(error);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA verification successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public String registerUserFromWeb(@ModelAttribute LoanUser user, @RequestParam(required = false) String secretCode) {
        System.out.println("--- REGISTRATION ATTEMPT ---");
        System.out.println("Email: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
        System.out.println("Security Key: " + secretCode);

        if (userRepository.existsByEmail(user.getEmail())) {
            System.out.println("Error: Email already exists!");
            return "redirect:/register?error=emailexists";
        }

        // Validate security key for employee roles
        String role = user.getRole();
        System.out.println("DEBUG: Role received: " + role);
        System.out.println("DEBUG: SecretCode received: " + secretCode);
        
        // Trim the secret code to remove any leading/trailing whitespace
        if (secretCode != null) {
            secretCode = secretCode.trim();
        }
        
        if (role != null && !role.isEmpty() && !role.equalsIgnoreCase("CUSTOMER")) {
            if (secretCode == null || secretCode.isEmpty()) {
                System.out.println("Error: Security key required for role: " + role);
                return "redirect:/register?error=invalidcode";
            }

            // Special bootstrap code for first admin registration
            final String BOOTSTRAP_ADMIN_CODE = "BAJAJ-ADMIN-BOOTSTRAP-2024";
            System.out.println("DEBUG: Bootstrap code: " + BOOTSTRAP_ADMIN_CODE);
            System.out.println("DEBUG: Code match: " + secretCode.equals(BOOTSTRAP_ADMIN_CODE));
            System.out.println("DEBUG: Role match: " + role.equalsIgnoreCase("ADMIN"));
            
            // Check if any admin already exists
            boolean adminExists = userRepository.existsByRole("ADMIN");
            System.out.println("DEBUG: Admin exists: " + adminExists);
            
            // List all admin users for debugging
            List<LoanUser> allAdmins = userRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .toList();
            System.out.println("DEBUG: Total admin users in database: " + allAdmins.size());
            for (LoanUser admin : allAdmins) {
                System.out.println("DEBUG: Admin found - Email: " + admin.getEmail() + ", ID: " + admin.getId());
            }
            
            // Check if this is the bootstrap admin registration
            if (role.equalsIgnoreCase("ADMIN") && secretCode.equals(BOOTSTRAP_ADMIN_CODE)) {
                if (adminExists) {
                    System.out.println("Error: Bootstrap code can only be used for first admin registration");
                    return "redirect:/register?error=invalidcode";
                }
                System.out.println("Bootstrap admin registration allowed for: " + user.getEmail());
            } else {
                // Validate the security key using SecurityKeyService
                if (!securityKeyService.validateSecurityKey(secretCode, role)) {
                    System.out.println("Error: Invalid or expired security key for role: " + role);
                    return "redirect:/register?error=invalidcode";
                }

                // Mark the key as used
                securityKeyService.markKeyAsUsed(secretCode, user.getEmail());
                System.out.println("Security key validated and marked as used for: " + user.getEmail());
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CUSTOMER"); // Default role
        }

        userRepository.save(user);
        System.out.println("User saved successfully in database!");

        // Registration ke baad seedha Login page par bhej dein success message ke sath
        return "redirect:/login?success";
    }

    // ==========================================
    // 2. REST API Endpoints (JSON / Postman / React)
    // ==========================================

    @ResponseBody
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerUserApi(@RequestBody LoanUser user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CUSTOMER");
        }

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully via API!");
    }

    @ResponseBody
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> authenticateUserApi(@RequestBody LoanUser loginRequest) {
        Optional<LoanUser> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isPresent()) {
            LoanUser user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String jwtToken = (jwtUtils != null) ? jwtUtils.generateJwtToken(user.getEmail()) : "JWT_DISABLED";

                Map<String, Object> response = new HashMap<>();
                response.put("token", jwtToken);
                response.put("email", user.getEmail());
                response.put("name", user.getFullName());
                response.put("role", user.getRole());

                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("Invalid email or password!");
    }
}