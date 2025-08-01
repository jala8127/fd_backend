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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

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

        userRepository.findByEmail(email).ifPresent(user -> {
            String signature = jwtUtil.extractSignature(jwt);
            user.setActiveToken(signature);
            userRepository.save(user);
        });

        return ResponseEntity.ok(Map.of("token", jwt, "email", email));
    }

    @PostMapping("/employees/login")
    public ResponseEntity<?> employeeLogin(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Employee emp = employeeRepository.findByEmail(email);
        if (emp == null || !passwordEncoder.matches(password, emp.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_CREDENTIALS", "message", "Invalid credentials"));
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + emp.getRole());
        final UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email,
                emp.getPassword(),
                Collections.singletonList(authority)
        );
        final String jwt = jwtUtil.generateToken(userDetails);

        String signature = jwtUtil.extractSignature(jwt);
        emp.setActiveToken(signature);
        employeeRepository.save(emp);

        return ResponseEntity.ok(Map.of("token", jwt, "email", email, "role", emp.getRole()));
    }

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

    @GetMapping("/rehash-employee-passwords")
    public ResponseEntity<Map<String, Object>> rehashPasswords() {
        List<Employee> employees = employeeRepository.findAll();
        int count = 0;
        for (Employee emp : employees) {
            String pwd = emp.getPassword();
            if (pwd != null && !pwd.startsWith("$2a$")) {
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

    @GetMapping("/rehash-user-mpins")
    public ResponseEntity<Map<String, Object>> rehashUserMpins() {
        // This finds all users in your database.
        List<User> users = userRepository.findAll();
        int count = 0;
        for (User user : users) {
            String mpin = user.getMpin();

            // This check is important! It ensures we only hash MPINs that are not already hashed.
            if (mpin != null && !mpin.startsWith("$2a$")) {
                // Use the secure password encoder to hash the plain-text MPIN.
                String hashedMpin = passwordEncoder.encode(mpin);
                user.setMpin(hashedMpin);
                userRepository.save(user);
                count++;
            }
        }
        return ResponseEntity.ok(Map.of(
                "message", "User MPINs were successfully re-hashed.",
                "updated_users", count
        ));
    }

    private String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }
}
