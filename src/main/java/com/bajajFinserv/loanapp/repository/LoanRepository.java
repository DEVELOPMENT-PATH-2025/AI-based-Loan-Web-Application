package com.bajajFinserv.loanapp.repository;

import com.bajajFinserv.loanapp.model.LoanApplication;
import com.bajajFinserv.loanapp.model.LoanUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<LoanApplication, Long> {

    // Sirf 'applicant' field ka use karein kyunki 'user' attribute nahi hai
    @Query("SELECT l FROM LoanApplication l WHERE l.applicant = :user")
    List<LoanApplication> findByUserOrApplicant(@Param("user") LoanUser user);

    List<LoanApplication> findByApplicant_Id(Long userId);

    List<LoanApplication> findByStatus(String status);

    @Query("SELECT SUM(l.loanAmount) FROM LoanApplication l")
    Double sumTotalLoanAmount();
}