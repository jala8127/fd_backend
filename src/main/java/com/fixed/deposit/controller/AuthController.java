package com.fixed.deposit.controller;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.EmployeeRepository;
import com.fixed.deposit.repository.UserRepository;
import com.fixed.deposit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        String otp = generateOtp();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Registration");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);

        return ResponseEntity.ok(otp);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        user.setRole("Customer");
        user.setStatus("ACTIVE"); // ensure user is active
        userRepository.save(user);
        return ResponseEntity.ok("Account created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String mpin = loginData.get("mpin");

        Optional<User> optional = userRepository.findByEmail(email);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }

        User customer = optional.get();

        if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is deactivated");
        }

        if (!customer.getMpin().equals(mpin)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid MPIN");
        }

        return ResponseEntity.ok(customer);
    }

    @PostMapping("/employees/login")
    public ResponseEntity<?> employeeLogin(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Employee emp = employeeRepository.findByEmail(email);
        if (emp == null || !passwordEncoder.matches(password, emp.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        Map<String, Object> response = Map.of(
                "id", emp.getEmp_Id(),
                "name", emp.getName(),
                "email", emp.getEmail(),
                "phone", emp.getPhone(),
                "role", emp.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userRepository.existsByPhone(phone);
        return ResponseEntity.ok(exists);
    }

    private String generateOtp() {
        int otp = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }
}