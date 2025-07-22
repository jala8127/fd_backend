package com.fixed.deposit.controller;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.saveEmployee(employee));
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/admin/total-deposits")
    public ResponseEntity<Map<String, Object>> getTotalDeposits() {
        // FIXED: Changed from long to double to match the service method's return type
        double total = employeeService.getTotalDeposits();
        return ResponseEntity.ok(Map.of("total", total));
    }

    @GetMapping("/admin/monthly-payouts")
    public ResponseEntity<Map<String, Object>> getMonthlyPayouts() {
        double total = employeeService.getMonthlyPayouts();
        return ResponseEntity.ok(Map.of("total", total));
    }

    @GetMapping("/admin/monthly-received")
    public ResponseEntity<Map<String, Object>> getMonthlyReceived() {
        double total = employeeService.getMonthlyReceived();
        return ResponseEntity.ok(Map.of("total", total));
    }

    @GetMapping("/admin/recent-transactions")
    public ResponseEntity<List<Map<String, Object>>> getRecentTransactions() {
        List<Map<String, Object>> transactions = employeeService.getRecentTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getLoggedInEmployeeProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("No employee is logged in.");
        }
        String email = principal.getName();
        Employee employee = employeeService.getEmployeeByEmail(email);
        if (employee == null) {
            return ResponseEntity.status(404).body("Employee profile not found.");
        }
        return ResponseEntity.ok(employee);
    }
}