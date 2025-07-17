package com.fixed.deposit.service;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
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

}