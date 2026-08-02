package com.bajajFinserv.loanapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "loan_applications")
@Data
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000.00", message = "Loan amount must be at least ₹10,000")
    @DecimalMax(value = "10000000.00", message = "Loan amount cannot exceed ₹10,000,000")
    private Double loanAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Tenure must be at least 3 months")
    @Max(value = 360, message = "Tenure cannot exceed 360 months")
    private Integer tenureMonths;

    @NotBlank(message = "Employment type is required")
    @Pattern(regexp = "^(SALARIED|SELF_EMPLOYED|BUSINESS)$", message = "Employment type must be SALARIED, SELF_EMPLOYED, or BUSINESS")
    private String employmentType;

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.00", message = "Annual income cannot be negative")
    private Double annualIncome;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.00", message = "Monthly income cannot be negative")
    private Double monthlyIncome;

    @NotNull(message = "Existing expenses is required")
    @DecimalMin(value = "0.00", message = "Existing expenses cannot be negative")
    private Double existingExpenses;

    // Status default "PENDING" set kar diya hai
    private String status = "PENDING";

    // Getters and Setters for status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Foreign key mapping (Database column: user_id)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private LoanUser applicant;

    // Convenience method taaki agar kahin .setUser() ya .getApplicant() use ho toh dono seamlessly chalein
    public LoanUser getUser() {
        return applicant;
    }

    public void setUser(LoanUser user) {
        this.applicant = user;
    }

    public LoanUser getApplicant() {
        return applicant;
    }

    public void setApplicant(LoanUser applicant) {
        this.applicant = applicant;
    }
}