package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Login Response DTO for authentication results
 * 
 * EN: Data Transfer Object for login response with JWT token and user details
 * PT: Objeto de Transferência de Dados para resposta de login com token JWT e detalhes do usuário
 * FR: Objet de transfert de données pour la réponse de connexion avec jeton JWT et détails utilisateur
 * ZH: 包含JWT令牌和用户详细信息的登录响应数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response data / Dados de resposta de login")
public class LoginResponseDTO {

    @Schema(description = "Authentication success status / Status de sucesso da autenticação", example = "true")
    private Boolean success;

    @Schema(description = "Response message / Mensagem de resposta", example = "Login successful")
    private String message;

    @Schema(description = "JWT access token / Token de acesso JWT")
    private String accessToken;

    @Schema(description = "JWT refresh token / Token de atualização JWT")
    private String refreshToken;

    @Schema(description = "Token type / Tipo de token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration time in seconds / Tempo de expiração do token em segundos", example = "3600")
    private Long expiresIn;

    @Schema(description = "Token expiration timestamp / Timestamp de expiração do token")
    private LocalDateTime expiresAt;

    @Schema(description = "User details / Detalhes do usuário")
    private UserDetailsDTO user;

    @Schema(description = "User permissions / Permissões do usuário")
    private List<String> permissions;

    @Schema(description = "User roles / Funções do usuário")
    private List<String> roles;

    @Schema(description = "Selected language / Idioma selecionado", example = "en-CA")
    private String language;

    @Schema(description = "Available languages / Idiomas disponíveis")
    private List<String> availableLanguages;
}

