package com.bajajFinserv.loanapp.controller;

import com.bajajFinserv.loanapp.model.LoanApplication;
import com.bajajFinserv.loanapp.model.LoanUser;
import com.bajajFinserv.loanapp.repository.LoanRepository;
import com.bajajFinserv.loanapp.repository.UserRepository;
import com.bajajFinserv.loanapp.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    // --- REST API Endpoints ---

    @ResponseBody
    @PostMapping("/api/loans/apply")
    public ResponseEntity<?> applyForLoanApi(@Valid @RequestBody LoanApplication loanApplication, 
                                             BindingResult bindingResult,
                                             Authentication authentication) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("details", bindingResult.getAllErrors());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (authentication == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String email = authentication.getName();
            LoanUser user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            loanApplication.setApplicant(user);
            loanApplication.setStatus("PENDING");
            LoanApplication savedLoan = loanRepository.save(loanApplication);
            return ResponseEntity.ok(savedLoan);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to apply for loan");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @ResponseBody
    @GetMapping("/api/loans/user/{userId}")
    public ResponseEntity<List<LoanApplication>> getLoansByUserApi(@PathVariable Long userId) {
        try {
            List<LoanApplication> loans = loanService.getLoansByUser(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Frontend JS ke liye active user ke loans fetch karne ka endpoint
    @ResponseBody
    @GetMapping("/api/loans/my-loans")
    public ResponseEntity<List<LoanApplication>> getMyLoans(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String email = authentication.getName();
            LoanUser user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                List<LoanApplication> loans = loanRepository.findByApplicant_Id(user.getId());
                return ResponseEntity.ok(loans);
            }
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Web Dashboard Form Submit Endpoint ---

    @PostMapping("/apply-loan")
    public String submitLoanApplication(@Valid @ModelAttribute LoanApplication loanApplication, 
                                        BindingResult bindingResult,
                                        Authentication authentication) {
        try {
            if (bindingResult.hasErrors()) {
                return "redirect:/customer/dashboard?error=validation";
            }

            if (authentication == null) {
                return "redirect:/login";
            }

            // 1. Current logged-in user ka email nikal kar user object fetch karein
            String email = authentication.getName();
            LoanUser user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                // 2. Loan application ke sath applicant ko zaroor map karein
                loanApplication.setApplicant(user);

                // 3. Default status set karein
                loanApplication.setStatus("PENDING");

                // 4. Database mein save karein
                loanRepository.save(loanApplication);
            }

            // 5. Wapas dashboard par redirect karein taaki naya loan list mein dikhe
            return "redirect:/customer/dashboard?success";
        } catch (Exception e) {
            return "redirect:/customer/dashboard?error";
        }
    }
}