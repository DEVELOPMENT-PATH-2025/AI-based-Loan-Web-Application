package com.bajajFinserv.loanapp.repository;

import com.bajajFinserv.loanapp.model.LoanUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Yeh import zaroori hai

public interface UserRepository extends JpaRepository<LoanUser, Long> {

    // Return type ko Optional<LoanUser> kar diya gaya hai
    Optional<LoanUser> findByEmail(String email);

    boolean existsByEmail(String email);
    
    boolean existsByRole(String role);
}