package com.bajajFinserv.loanapp.service;

import com.bajajFinserv.loanapp.model.LoanApplication;
import com.bajajFinserv.loanapp.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;

    // Constructor Injection (Autowired likhne ki bhi zaroorat nahi hai modern Spring mein)
    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public LoanApplication applyLoan(LoanApplication loanApplication) {
        return loanRepository.save(loanApplication);
    }

    public List<LoanApplication> getLoansByUser(Long userId) {
        return loanRepository.findByApplicant_Id(userId);
    }
}