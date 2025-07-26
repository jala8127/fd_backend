package com.fixed.deposit.controller;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.PaymentsRepository;
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
    @PostMapping("/add-customer")
    public ResponseEntity<?> addCustomer(@RequestBody User newCustomer) {
        employeeService.createCustomer(newCustomer);
        return ResponseEntity.ok(Map.of("message", "Customer created successfully"));
    }
    @PostMapping("/manual-deposit")
    public ResponseEntity<?> createManualDeposit(@RequestBody Map<String, Object> depositPayload) {
        try {
            employeeService.createManualDeposit(depositPayload);
            return ResponseEntity.ok(Map.of("message", "Deposit created successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/admin/chart-data")
    public ResponseEntity<List<PaymentsRepository.ChartDataProjection>> getChartData() {
        List<PaymentsRepository.ChartDataProjection> chartData = employeeService.getMonthlyChartData();
        return ResponseEntity.ok(chartData);
    }

}