package com.fixed.deposit.controller;

import com.fixed.deposit.config.JwtUtil;
import com.fixed.deposit.model.Employee;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.EmployeeRepository;
import com.fixed.deposit.repository.UserRepository;
import com.fixed.deposit.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder; // Import the generic interface
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JavaMailSender mailSender;

    // FIXED: Inject the generic PasswordEncoder interface. Spring will provide the primary (BCrypt) bean.
    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String mpin = loginData.get("mpin");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, mpin)
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_CREDENTIALS", "message", "Incorrect email or MPIN"));
        }

        final UserDetails userDetails = authService.loadUserByUsername(email);
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", jwt, "email", email));
    }

    @PostMapping("/employees/login")
    public ResponseEntity<?> employeeLogin(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Employee emp = employeeRepository.findByEmail(email);
        // This will now use the correctly injected BCrypt encoder
        if (emp == null || !passwordEncoder.matches(password, emp.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_CREDENTIALS", "message", "Invalid credentials"));
        }

        final UserDetails userDetails = new org.springframework.security.core.userdetails.User(email, emp.getPassword(), new ArrayList<>());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", jwt, "email", email, "role", emp.getRole()));
    }

    @GetMapping("/rehash-employee-passwords")
    public ResponseEntity<Map<String, Object>> rehashPasswords() {
        List<Employee> employees = employeeRepository.findAll();
        int count = 0;
        for (Employee emp : employees) {
            String pwd = emp.getPassword();
            if (pwd != null && !pwd.startsWith("$2a$")) {
                // This will now use the correctly injected BCrypt encoder
                String hashed = passwordEncoder.encode(pwd);
                emp.setPassword(hashed);
                employeeRepository.save(emp);
                count++;
            }
        }
        return ResponseEntity.ok(Map.of(
                "message", "Passwords rehashed successfully",
                "updated", count
        ));
    }

    // --- All other methods below remain unchanged ---
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "EMAIL_EXISTS", "message", "Email already registered"));
        }
        String otp = generateOtp();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Registration");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
        return ResponseEntity.ok(Map.of("otp", otp, "message", "OTP sent successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "EMAIL_EXISTS", "message", "Email already exists"));
        }
        user.setRole("Customer");
        user.setStatus("ACTIVE");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Account created successfully"));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@PathVariable String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String phone) {
        boolean exists = userRepository.existsByPhone(phone);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    private String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }
}
