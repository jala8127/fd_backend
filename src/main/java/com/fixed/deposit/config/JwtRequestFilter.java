package com.fixed.deposit.config;

import com.fixed.deposit.model.Employee;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.EmployeeRepository;
import com.fixed.deposit.repository.UserRepository;
import com.fixed.deposit.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT validation error: Token has expired. {}", e.getMessage());
            } catch (SignatureException e) {
                logger.warn("JWT validation error: Signature validation failed. {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.warn("JWT validation error: Invalid token format. {}", e.getMessage());
            } catch (UnsupportedJwtException e) {
                logger.warn("JWT validation error: Token type is not supported. {}", e.getMessage());
            } catch (Exception e) {
                logger.error("An unexpected error occurred during JWT parsing.", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.authService.loadUserByUsername(username);

            boolean isTokenValid = jwtUtil.validateToken(jwt, userDetails);
            boolean isTokenActive = isTokenTheActiveOne(jwt, userDetails);

            if (isTokenValid && isTokenActive) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                logger.warn("Authentication Failed for user '{}'. Token Valid: {}, Token Active: {}", username, isTokenValid, isTokenActive);
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isTokenTheActiveOne(String token, UserDetails userDetails) {
        String incomingSignature = jwtUtil.extractSignature(token);
        if (incomingSignature == null) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isPresent()) {
            String activeToken = userOpt.get().getActiveToken();
            if (activeToken == null) {
                logger.warn("Active token check failed: No active token found in DB for user '{}'", userDetails.getUsername());
                return false;
            }
            return incomingSignature.equals(activeToken);
        }

        Employee employee = employeeRepository.findByEmail(userDetails.getUsername());
        if (employee != null) {
            String activeToken = employee.getActiveToken();
            if (activeToken == null) {
                logger.warn("Active token check failed: No active token found in DB for employee '{}'", userDetails.getUsername());
                return false;
            }
            return incomingSignature.equals(activeToken);
        }

        return false;
    }
}