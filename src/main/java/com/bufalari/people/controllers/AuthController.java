package com.bufalari.people.controllers;

import com.bufalari.people.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller using existing JWT infrastructure
 * Provides demo login functionality for system demonstration
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final JwtService jwtService;

    /**
     * Demo login endpoint using existing JWT service
     * Credentials: admin/admin for demonstration
     */
    @PostMapping("/login")
    @Operation(summary = "Login with demo credentials", description = "Demo login endpoint (admin/admin)")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        log.info("Login attempt for username: {}", username);
        
        // Demo authentication - in production this would validate against database
        if ("admin".equals(username) && "admin".equals(password)) {
            // Create UserDetails using existing structure
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("") // Not used for JWT
                    .authorities(Collections.emptyList())
                    .build();
            
            // Generate token using existing JwtService
            String token = jwtService.generateToken(userDetails);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("message", "Login successful");
            response.put("expiresIn", 36000); // 10 hours in seconds
            
            log.info("Login successful for user: {}", username);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Login failed for username: {}", username);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            errorResponse.put("message", "Please use admin/admin for demo");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    /**
     * Get supported languages for internationalization
     */
    @GetMapping("/languages")
    @Operation(summary = "Get supported languages", description = "Returns list of supported languages")
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        Map<String, Object> response = new HashMap<>();
        response.put("supported", new Object[]{
            Map.of("code", "en-CA", "name", "English (Canada)", "default", true),
            Map.of("code", "pt-BR", "name", "Português (Brasil)", "default", false),
            Map.of("code", "hi-IN", "name", "हिन्दी (भारत)", "default", false),
            Map.of("code", "zh-CN", "name", "中文 (简体)", "default", false)
        });
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if authentication service is running")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "create-people-auth");
        return ResponseEntity.ok(response);
    }
}

