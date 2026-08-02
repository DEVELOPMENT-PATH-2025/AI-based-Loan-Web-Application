package com.bajajFinserv.loanapp.controller;

import com.bajajFinserv.loanapp.model.LoanApplication;
import com.bajajFinserv.loanapp.model.LoanUser;
import com.bajajFinserv.loanapp.model.SecurityKey;
import com.bajajFinserv.loanapp.repository.LoanRepository;
import com.bajajFinserv.loanapp.repository.UserRepository;
import com.bajajFinserv.loanapp.service.SecurityKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityKeyService securityKeyService;

    @GetMapping("/dashboard")
    public String redirectToDashboard(Authentication authentication) {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN") || roles.contains("ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (roles.contains("ROLE_MANAGER") || roles.contains("MANAGER")) {
            return "redirect:/manager/dashboard";
        } else if (roles.contains("ROLE_OFFICER") || roles.contains("OFFICER")) {
            return "redirect:/officer/dashboard";
        } else {
            return "redirect:/customer/dashboard";
        }
    }

    @GetMapping("/customer/dashboard")
    public String showCustomerDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        LoanUser user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            model.addAttribute("fullName", user.getFullName());
            model.addAttribute("email", user.getEmail());

            List<LoanApplication> userLoans = loanRepository.findByUserOrApplicant(user);
            model.addAttribute("userLoans", userLoans);
        } else {
            model.addAttribute("fullName", "User");
            model.addAttribute("email", email);
            model.addAttribute("userLoans", List.of());
        }

        return "customer/Userdashboard";
    }

    @PostMapping("/update-profile")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        LoanUser user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/customer/dashboard";
        }

        // Update basic information
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);

        // Handle password change if provided
        if (currentPassword != null && !currentPassword.isEmpty()) {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/customer/dashboard";
            }

            if (newPassword == null || newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/customer/dashboard";
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
                return "redirect:/customer/dashboard";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        return "redirect:/customer/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard() {
        return "admin/Admindashboard";
    }

    @GetMapping("/manager/dashboard")
    public String showManagerDashboard() {
        return "manager/Managerdashboard";
    }

    @GetMapping("/officer/dashboard")
    public String showOfficerDashboard() {
        return "officer/Officerdashboard";
    }

    // ==========================================
    // Security Key Management (Admin Only)
    // ==========================================

    @GetMapping("/admin/security-keys")
    public String showSecurityKeysPage(Model model, Authentication authentication) {
        List<SecurityKey> allKeys = securityKeyService.getAllSecurityKeys();
        List<SecurityKey> availableKeys = securityKeyService.getAvailableSecurityKeys();
        
        model.addAttribute("allKeys", allKeys);
        model.addAttribute("availableKeys", availableKeys);
        model.addAttribute("totalKeys", allKeys.size());
        model.addAttribute("activeKeys", allKeys.stream().filter(SecurityKey::isActive).count());
        model.addAttribute("usedKeys", allKeys.stream().filter(SecurityKey::isUsed).count());
        
        return "admin/security-keys";
    }

    @ResponseBody
    @PostMapping("/admin/security-keys/generate")
    public ResponseEntity<?> generateSecurityKey(
            @RequestParam String role,
            @RequestParam(defaultValue = "1") int maxUses,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        
        try {
            String createdBy = authentication.getName();
            SecurityKey securityKey = securityKeyService.createSecurityKey(role, createdBy, maxUses, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Security key generated successfully");
            response.put("key", securityKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to generate security key: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @ResponseBody
    @PostMapping("/admin/security-keys/deactivate/{id}")
    public ResponseEntity<?> deactivateSecurityKey(@PathVariable Long id, Authentication authentication) {
        try {
            securityKeyService.deactivateSecurityKey(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Security key deactivated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to deactivate security key: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @ResponseBody
    @DeleteMapping("/admin/security-keys/{id}")
    public ResponseEntity<?> deleteSecurityKey(@PathVariable Long id, Authentication authentication) {
        try {
            securityKeyService.deleteSecurityKey(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Security key deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete security key: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @ResponseBody
    @GetMapping("/admin/security-keys/api")
    public ResponseEntity<List<SecurityKey>> getSecurityKeysApi() {
        try {
            return ResponseEntity.ok(securityKeyService.getAllSecurityKeys());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}