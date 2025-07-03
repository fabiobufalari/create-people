package com.bufalari.people.controllers;

import com.bufalari.people.dto.LoginRequestDTO;
import com.bufalari.people.dto.LoginResponseDTO;
import com.bufalari.people.dto.UserDetailsDTO;
import com.bufalari.people.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller for Construction Hub Financial System
 * Handles user authentication and JWT token generation
 * 
 * EN: Provides authentication endpoints for user login and token validation
 * PT: Fornece endpoints de autenticação para login de usuário e validação de token
 * FR: Fournit des points de terminaison d'authentification pour la connexion utilisateur et la validation de jeton
 * ZH: 为用户登录和令牌验证提供身份验证端点
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for user login and token management")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * Health check endpoint for authentication service
     */
    @GetMapping("/health")
    @Operation(summary = "Authentication service health check", 
               description = "Verifica o status de saúde do serviço de autenticação")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("service", "authentication-service");
        healthStatus.put("status", "healthy");
        healthStatus.put("version", "1.0.0");
        return ResponseEntity.ok(healthStatus);
    }

    /**
     * User login endpoint
     * 
     * EN: Authenticates user credentials and returns JWT token
     * PT: Autentica credenciais do usuário e retorna token JWT
     * FR: Authentifie les informations d'identification de l'utilisateur et renvoie le jeton JWT
     * ZH: 验证用户凭据并返回JWT令牌
     */
    @PostMapping("/login")
    @Operation(summary = "User login", 
               description = "Authenticate user and return JWT token / Autentica usuário e retorna token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                                     schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        try {
            LoginResponseDTO response = authService.authenticate(loginRequest);
            log.info("Login successful for username: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("Login failed for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDTO.builder()
                            .success(false)
                            .message("Invalid username or password")
                            .build());
        }
    }

    /**
     * Token validation endpoint
     * 
     * EN: Validates JWT token and returns user details
     * PT: Valida token JWT e retorna detalhes do usuário
     * FR: Valide le jeton JWT et renvoie les détails de l'utilisateur
     * ZH: 验证JWT令牌并返回用户详细信息
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", 
               description = "Get current authenticated user details / Obter detalhes do usuário autenticado atual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                                     schema = @Schema(implementation = UserDetailsDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<UserDetailsDTO> getCurrentUser(@RequestHeader("Authorization") String authorizationHeader) {
        log.debug("Getting current user details");
        
        try {
            UserDetailsDTO userDetails = authService.getCurrentUser(authorizationHeader);
            return ResponseEntity.ok(userDetails);
            
        } catch (Exception e) {
            log.warn("Failed to get current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Token refresh endpoint
     * 
     * EN: Refreshes JWT token for authenticated user
     * PT: Atualiza token JWT para usuário autenticado
     * FR: Actualise le jeton JWT pour l'utilisateur authentifié
     * ZH: 为经过身份验证的用户刷新JWT令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", 
               description = "Refresh JWT token / Atualizar token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                                     schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        log.debug("Refreshing token");
        
        try {
            LoginResponseDTO response = authService.refreshToken(authorizationHeader);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("Failed to refresh token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDTO.builder()
                            .success(false)
                            .message("Invalid or expired token")
                            .build());
        }
    }

    /**
     * Logout endpoint
     * 
     * EN: Logs out user and invalidates token
     * PT: Faz logout do usuário e invalida token
     * FR: Déconnecte l'utilisateur et invalide le jeton
     * ZH: 注销用户并使令牌无效
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", 
               description = "Logout user and invalidate token / Fazer logout do usuário e invalidar token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        log.debug("User logout");
        
        try {
            authService.logout(authorizationHeader);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Logout successful");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("Failed to logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Available languages endpoint
     * 
     * EN: Returns list of supported languages for the login screen
     * PT: Retorna lista de idiomas suportados para a tela de login
     * FR: Renvoie la liste des langues prises en charge pour l'écran de connexion
     * ZH: 返回登录屏幕支持的语言列表
     */
    @GetMapping("/languages")
    @Operation(summary = "Get supported languages", 
               description = "Get list of supported languages / Obter lista de idiomas suportados")
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        Map<String, Object> response = new HashMap<>();
        response.put("default_language", "en-CA");
        response.put("supported_languages", new String[]{
            "en-CA", // English (Canadian)
            "pt-BR", // Portuguese (Brazilian)
            "fr-CA", // French (Canadian)
            "zh-CN"  // Chinese (Simplified)
        });
        
        Map<String, String> languageNames = new HashMap<>();
        languageNames.put("en-CA", "English (Canada)");
        languageNames.put("pt-BR", "Português (Brasil)");
        languageNames.put("fr-CA", "Français (Canada)");
        languageNames.put("zh-CN", "中文 (简体)");
        
        response.put("language_names", languageNames);
        
        return ResponseEntity.ok(response);
    }
}

