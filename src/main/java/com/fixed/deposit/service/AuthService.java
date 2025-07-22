package com.fixed.deposit.service;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.EmployeeRepository;
import com.fixed.deposit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getMpin(),
                    Collections.singletonList(authority)
            );
        }

        Employee employee = employeeRepository.findByEmail(email);
        if (employee != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + employee.getRole());
            return new org.springframework.security.core.userdetails.User(
                    employee.getEmail(),
                    employee.getPassword(),
                    Collections.singletonList(authority)
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
