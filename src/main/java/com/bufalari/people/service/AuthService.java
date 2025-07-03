package com.bufalari.people.service;

import com.bufalari.people.dto.LoginRequestDTO;
import com.bufalari.people.dto.LoginResponseDTO;
import com.bufalari.people.dto.UserDetailsDTO;
import com.bufalari.people.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication Service for Construction Hub Financial System
 * 
 * EN: Handles user authentication, JWT token generation, and demo user management
 * PT: Gerencia autenticação de usuário, geração de token JWT e gerenciamento de usuário demo
 * FR: Gère l'authentification des utilisateurs, la génération de jetons JWT et la gestion des utilisateurs de démonstration
 * ZH: 处理用户身份验证、JWT令牌生成和演示用户管理
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Demo user credentials - In production, this would be in a database
    private static final String DEMO_USERNAME = "admin";
    private static final String DEMO_PASSWORD = "admin";
    private static final String DEMO_EMAIL = "admin@constructionhub.com";
    private static final String DEMO_FIRST_NAME = "Admin";
    private static final String DEMO_LAST_NAME = "User";
    private static final List<String> DEMO_ROLES = Arrays.asList("ADMIN", "MANAGER", "ACCOUNTANT", "FINANCIAL_VIEWER");
    private static final List<String> DEMO_PERMISSIONS = Arrays.asList(
        "CREATE_PAYABLE", "READ_PAYABLE", "UPDATE_PAYABLE", "DELETE_PAYABLE",
        "CREATE_RECEIVABLE", "READ_RECEIVABLE", "UPDATE_RECEIVABLE", "DELETE_RECEIVABLE",
        "CREATE_PROJECT", "READ_PROJECT", "UPDATE_PROJECT", "DELETE_PROJECT",
        "CREATE_PERSON", "READ_PERSON", "UPDATE_PERSON", "DELETE_PERSON",
        "CREATE_COMPANY", "READ_COMPANY", "UPDATE_COMPANY", "DELETE_COMPANY",
        "VIEW_REPORTS", "EXPORT_DATA", "MANAGE_USERS", "SYSTEM_ADMIN"
    );

    // Supported languages
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
        "en-CA", "pt-BR", "fr-CA", "zh-CN"
    );

    // In-memory token blacklist for logout (in production, use Redis or database)
    private final Map<String, LocalDateTime> tokenBlacklist = new HashMap<>();

    /**
     * Authenticate user credentials and generate JWT token
     * 
     * EN: Validates user credentials and returns authentication response with JWT token
     * PT: Valida credenciais do usuário e retorna resposta de autenticação com token JWT
     * FR: Valide les informations d'identification de l'utilisateur et renvoie une réponse d'authentification avec un jeton JWT
     * ZH: 验证用户凭据并返回带有JWT令牌的身份验证响应
     */
    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());

        // Validate demo user credentials
        if (!DEMO_USERNAME.equals(loginRequest.getUsername()) || 
            !DEMO_PASSWORD.equals(loginRequest.getPassword())) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        // Create user details for demo user
        UserDetailsDTO userDetails = createDemoUserDetails();

        // Generate JWT tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        // Calculate expiration
        long expiresInSeconds = jwtService.getExpirationTime() / 1000;
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);

        // Set language preference
        String language = loginRequest.getLanguage();
        if (language == null || !SUPPORTED_LANGUAGES.contains(language)) {
            language = "en-CA"; // Default to English (Canadian)
        }

        log.info("Authentication successful for user: {}", loginRequest.getUsername());

        return LoginResponseDTO.builder()
                .success(true)
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresInSeconds)
                .expiresAt(expiresAt)
                .user(userDetails)
                .permissions(DEMO_PERMISSIONS)
                .roles(DEMO_ROLES)
                .language(language)
                .availableLanguages(SUPPORTED_LANGUAGES)
                .build();
    }

    /**
     * Get current user details from JWT token
     * 
     * EN: Extracts and validates user details from JWT token
     * PT: Extrai e valida detalhes do usuário do token JWT
     * FR: Extrait et valide les détails de l'utilisateur à partir du jeton JWT
     * ZH: 从JWT令牌中提取和验证用户详细信息
     */
    public UserDetailsDTO getCurrentUser(String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        
        if (isTokenBlacklisted(token)) {
            throw new BadCredentialsException("Token has been invalidated");
        }

        if (!jwtService.isTokenValid(token)) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        String username = jwtService.extractUsername(token);
        
        if (DEMO_USERNAME.equals(username)) {
            return createDemoUserDetails();
        }

        throw new BadCredentialsException("User not found");
    }

    /**
     * Refresh JWT token
     * 
     * EN: Generates new access token using refresh token
     * PT: Gera novo token de acesso usando token de atualização
     * FR: Génère un nouveau jeton d'accès en utilisant le jeton de rafraîchissement
     * ZH: 使用刷新令牌生成新的访问令牌
     */
    public LoginResponseDTO refreshToken(String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        
        if (isTokenBlacklisted(token)) {
            throw new BadCredentialsException("Token has been invalidated");
        }

        if (!jwtService.isTokenValid(token)) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        String username = jwtService.extractUsername(token);
        
        if (DEMO_USERNAME.equals(username)) {
            UserDetailsDTO userDetails = createDemoUserDetails();
            
            String newAccessToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);
            
            long expiresInSeconds = jwtService.getExpirationTime() / 1000;
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);

            return LoginResponseDTO.builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(expiresInSeconds)
                    .expiresAt(expiresAt)
                    .user(userDetails)
                    .permissions(DEMO_PERMISSIONS)
                    .roles(DEMO_ROLES)
                    .language("en-CA")
                    .availableLanguages(SUPPORTED_LANGUAGES)
                    .build();
        }

        throw new BadCredentialsException("User not found");
    }

    /**
     * Logout user and invalidate token
     * 
     * EN: Adds token to blacklist to prevent further use
     * PT: Adiciona token à lista negra para prevenir uso futuro
     * FR: Ajoute le jeton à la liste noire pour empêcher toute utilisation ultérieure
     * ZH: 将令牌添加到黑名单以防止进一步使用
     */
    public void logout(String authorizationHeader) {
        String token = extractTokenFromHeader(authorizationHeader);
        
        // Add token to blacklist
        tokenBlacklist.put(token, LocalDateTime.now());
        
        // Clean up expired tokens from blacklist (simple cleanup)
        cleanupExpiredTokens();
        
        log.info("User logged out successfully");
    }

    /**
     * Create demo user details
     */
    private UserDetailsDTO createDemoUserDetails() {
        return UserDetailsDTO.builder()
                .username(DEMO_USERNAME)
                .email(DEMO_EMAIL)
                .firstName(DEMO_FIRST_NAME)
                .lastName(DEMO_LAST_NAME)
                .fullName(DEMO_FIRST_NAME + " " + DEMO_LAST_NAME)
                .roles(DEMO_ROLES)
                .permissions(DEMO_PERMISSIONS)
                .isActive(true)
                .build();
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }

    /**
     * Check if token is blacklisted
     */
    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.containsKey(token);
    }

    /**
     * Clean up expired tokens from blacklist
     */
    private void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }
}

