package com.fixed.deposit.service;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepositsRepository depositsRepository;

    @Autowired
    private DepositsService depositsService;

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private PaymentsService paymentsService;

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private UserRepository userRepository;


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

    public Employee saveEmployee(Employee emp) {
        if (emp.getPassword() != null && !emp.getPassword().isEmpty()) {
            emp.setPassword(passwordEncoder.encode(emp.getPassword()));
        }
        return repository.save(emp);
    }

    public Employee getEmployeeByEmail(String email) {
        return repository.findByEmail(email);
    }

    public double getTotalDeposits() {
        return depositsRepository.sumAmountByStatus("ACTIVE");
    }

    public double getMonthlyPayouts() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        return payoutRepository.sumAmountPaidBetweenDates(startDate, endDate);
    }

    public double getMonthlyReceived() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        return paymentsRepository.sumAmountByStatusBetweenDates("SUCCESS", startOfMonth, endOfMonth);
    }

    public List<Map<String, Object>> getRecentTransactions() {
        return paymentsRepository.findTop5ByStatusOrderByPaymentDateDesc("SUCCESS")
                .stream()
                .map(payment -> {
                    Map<String, Object> transactionMap = new HashMap<>();
                    Optional<User> userOpt = userRepository.findByEmail(payment.getUserEmail());
                    Long userId = userOpt.map(User::getUserId).orElse(null);
                    transactionMap.put("customer", userId);
                    transactionMap.put("amount", payment.getAmount());
                    transactionMap.put("mode", payment.getPaymentMode());
                    transactionMap.put("status", payment.getStatus());
                    transactionMap.put("date", payment.getPaymentDate());
                    return transactionMap;
                })
                .collect(Collectors.toList());
    }
    public void createCustomer(User newCustomer) {
        if (userRepository.existsByEmail(newCustomer.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A customer with this email already exists.");
        }
        if (userRepository.existsByPhone(newCustomer.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A customer with this phone number already exists.");
        }

        newCustomer.setMpin(passwordEncoder.encode(newCustomer.getMpin()));

        newCustomer.setRole("Customer");
        newCustomer.setStatus("ACTIVE");

        userRepository.save(newCustomer);
    }
    @Transactional
    public void createManualDeposit(Map<String, Object> body) {
        paymentsService.createPayment(body);
    }
    public List<PaymentsRepository.ChartDataProjection> getMonthlyChartData() {
        return paymentsRepository.getMonthlyChartData();
    }
}