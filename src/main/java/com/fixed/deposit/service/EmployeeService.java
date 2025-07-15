package com.fixed.deposit.service;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Employee updateEmployee(Long id, Employee emp) {
        Employee existing = repository.findById(id).orElseThrow();
        existing.setName(emp.getName());
        existing.setEmail(emp.getEmail());
        existing.setPhone(emp.getPhone());
        existing.setDepartment(emp.getDepartment());
        existing.setRole(emp.getRole());
        existing.setPhotoUrl(emp.getPhotoUrl());
        return repository.save(existing);
    }

    public void deleteEmployee(Long id) {
        repository.deleteById(id);
    }
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Employee saveEmployee(Employee emp) {
        if (emp.getPassword() != null && !emp.getPassword().isEmpty()) {
            emp.setPassword(passwordEncoder.encode(emp.getPassword()));
        }
        return repository.save(emp);
    }
    @GetMapping("/rehash-employee-passwords")
    public ResponseEntity<String> rehashPasswords() {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            String pwd = emp.getPassword();
            if (!pwd.startsWith("$2a$")) { // only hash if not already hashed
                String hashed = passwordEncoder.encode(pwd);
                emp.setPassword(hashed);
                employeeRepository.save(emp); // persist the change
            }
        }
        return ResponseEntity.ok("Passwords rehashed successfully.");
    }
}