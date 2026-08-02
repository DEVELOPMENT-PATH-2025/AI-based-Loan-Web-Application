package com.bajajFinserv.loanapp.controller;

import com.bajajFinserv.loanapp.model.LoanUser;
import com.bajajFinserv.loanapp.model.LoanApplication;
import com.bajajFinserv.loanapp.repository.UserRepository;
import com.bajajFinserv.loanapp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    // --- ADMIN APIs ---
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        try {
            long totalUsers = userRepository.count();
            Double totalLoanVolume = loanRepository.sumTotalLoanAmount();
            if (totalLoanVolume == null) totalLoanVolume = 0.0;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalLoanVolume", totalLoanVolume);
            stats.put("uptime", "99.9%");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch admin statistics");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<LoanUser>> getAllUsers() {
        try {
            return ResponseEntity.ok(userRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- OFFICER APIs ---
    @GetMapping("/officer/pending-loans")
    public ResponseEntity<List<LoanApplication>> getPendingLoansForOfficer() {
        try {
            return ResponseEntity.ok(loanRepository.findByStatus("PENDING"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/officer/verify/{id}")
    public ResponseEntity<Map<String, String>> verifyLoan(@PathVariable Long id) {
        try {
            LoanApplication loan = loanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
            
            if (!"PENDING".equals(loan.getStatus())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Loan can only be verified when in PENDING status");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            loan.setStatus("VERIFIED_BY_OFFICER");
            loanRepository.save(loan);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Loan verified and forwarded successfully");
            response.put("loanId", id.toString());
            response.put("newStatus", loan.getStatus());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to verify loan");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // --- MANAGER APIs ---
    @GetMapping("/manager/pending-approvals")
    public ResponseEntity<List<LoanApplication>> getPendingApprovalsForManager() {
        try {
            return ResponseEntity.ok(loanRepository.findByStatus("VERIFIED_BY_OFFICER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/manager/decision/{id}")
    public ResponseEntity<Map<String, String>> managerDecision(@PathVariable Long id, @RequestParam String status) {
        try {
            LoanApplication loan = loanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + id));
            
            if (!"VERIFIED_BY_OFFICER".equals(loan.getStatus())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Loan can only be decided when verified by officer");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if ("APPROVE".equalsIgnoreCase(status)) {
                loan.setStatus("APPROVED");
            } else if ("REJECT".equalsIgnoreCase(status)) {
                loan.setStatus("REJECTED");
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status. Use APPROVE or REJECT");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            loanRepository.save(loan);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Loan status updated successfully");
            response.put("loanId", id.toString());
            response.put("newStatus", loan.getStatus());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update loan decision");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}