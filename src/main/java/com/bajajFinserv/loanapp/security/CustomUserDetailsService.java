package com.bajajFinserv.loanapp.security; // Apne package ke hisab se adjust kar lein

import com.bajajFinserv.loanapp.model.LoanUser;
import com.bajajFinserv.loanapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LoanUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // Yeh encoded password hoga jo database mein hai
                .roles(user.getRole() != null ? user.getRole().toUpperCase() : "CUSTOMER")
                .build();
    }
}