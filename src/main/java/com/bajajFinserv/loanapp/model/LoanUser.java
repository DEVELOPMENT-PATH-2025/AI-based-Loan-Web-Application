package com.bajajFinserv.loanapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class LoanUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Roles: CUSTOMER, OFFICER, MANAGER, ADMIN
    @Pattern(regexp = "^(CUSTOMER|OFFICER|MANAGER|ADMIN)$", message = "Role must be CUSTOMER, OFFICER, MANAGER, or ADMIN")
    private String role;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    // Password reset fields
    @Column(length = 255)
    private String resetToken;

    private java.time.LocalDateTime resetTokenExpiry;

    // 2FA fields
    private boolean twoFactorEnabled = false;
    
    @Column(length = 255)
    private String twoFactorSecret;
    
    @Column(length = 1000)
    private String backupCodes;

    // Explicit Getters and Setters (Added getId and setId)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getResetToken() {
        return resetToken;
    }
    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public java.time.LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }
    public void setResetTokenExpiry(java.time.LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }
    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public String getBackupCodes() {
        return backupCodes;
    }
    public void setBackupCodes(String backupCodes) {
        this.backupCodes = backupCodes;
    }
}