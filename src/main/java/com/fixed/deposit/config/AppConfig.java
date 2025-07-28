package com.fixed.deposit.config;

import com.fixed.deposit.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class AppConfig implements WebMvcConfigurer {

    // 👇 ADDED: Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private AuthService authService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(authService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Security Filter Chain...");
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> {
                    logger.info("Applying security rules...");
                    authz
                            // 1. PUBLIC ENDPOINTS (No authentication needed)
                            .requestMatchers("/api/auth/**", "/uploads/**").permitAll()

                            // Rule for support endpoints
                            .requestMatchers("/api/support/**").hasAnyRole("Admin","Manager","Employee", "Customer")

                            // 2. SPECIFIC ENDPOINTS FOR CUSTOMERS (To override broader admin rules)
                            .requestMatchers("/api/schemes/help-info").hasRole("Customer")
                            .requestMatchers("/api/help/**").hasRole("Customer")
                            .requestMatchers(HttpMethod.GET, "/api/kyc/my-status").hasRole("Customer")
                            .requestMatchers(HttpMethod.GET, "/api/schemes/user/active").hasRole("Customer")
                            .requestMatchers(HttpMethod.POST, "/api/kyc/submit").hasRole("Customer")
                            .requestMatchers("/api/deposits/my-deposits").hasRole("Customer")
                            .requestMatchers(HttpMethod.PUT, "/api/user/update-field").hasRole("Customer")

                            // 3. ENDPOINTS SHARED BY CUSTOMERS AND EMPLOYEES
                            .requestMatchers(HttpMethod.PUT, "/api/deposits/close/*").hasAnyRole("Admin", "Manager", "employee", "Customer")
                            .requestMatchers(HttpMethod.GET, "/api/deposits/preview-close/*").hasAnyRole("Admin", "Manager", "employee", "Customer")

                            // 4. ADMIN & EMPLOYEE ONLY ENDPOINTS (Broader rules)
                            .requestMatchers("/api/admin/**", "/api/employees/**", "/api/kyc/**", "/api/schemes/**").hasAnyRole("Admin", "Manager", "employee")
                            .requestMatchers("/api/user/all", "/api/user/users/*/soft-delete").hasAnyRole("Admin", "Manager", "employee")
                            .requestMatchers("/api/deposits/all", "/api/payments/all", "/api/payouts").hasAnyRole("Admin", "Manager", "employee")
                            .requestMatchers("/api/auth/rehash-user-mpins").hasRole("Admin")

                            // 5. GENERAL AUTHENTICATED CUSTOMER ENDPOINTS
                            .requestMatchers("/api/user/**", "/api/deposits/**", "/api/payments/**", "/api/payouts/**").hasRole("Customer")

                            // 6. CATCH-ALL (Any other request requires authentication)
                            .anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("Security Filter Chain configuration complete.");
        return http.build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configuring CORS for allowed origins...");
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        logger.info("Configuring resource handler for /uploads/**");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
