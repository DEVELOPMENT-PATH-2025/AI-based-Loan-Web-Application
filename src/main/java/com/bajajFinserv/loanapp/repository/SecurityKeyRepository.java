package com.bajajFinserv.loanapp.repository;

import com.bajajFinserv.loanapp.model.SecurityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityKeyRepository extends JpaRepository<SecurityKey, Long> {

    Optional<SecurityKey> findByKeyCode(String keyCode);

    List<SecurityKey> findByRole(String role);

    List<SecurityKey> findByActiveTrue();

    List<SecurityKey> findByActiveTrueAndUsedFalse();

    List<SecurityKey> findByCreatedBy(String createdBy);

    boolean existsByKeyCode(String keyCode);
}
