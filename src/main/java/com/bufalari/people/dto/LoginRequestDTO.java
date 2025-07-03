package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO for user authentication
 * 
 * EN: Data Transfer Object for user login requests
 * PT: Objeto de Transferência de Dados para requisições de login de usuário
 * FR: Objet de transfert de données pour les demandes de connexion utilisateur
 * ZH: 用户登录请求的数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request data / Dados de requisição de login")
public class LoginRequestDTO {

    @NotBlank(message = "Username is required / Nome de usuário é obrigatório")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters / Nome de usuário deve ter entre 3 e 50 caracteres")
    @Schema(description = "Username / Nome de usuário", example = "admin", required = true)
    private String username;

    @NotBlank(message = "Password is required / Senha é obrigatória")
    @Size(min = 3, max = 100, message = "Password must be between 3 and 100 characters / Senha deve ter entre 3 e 100 caracteres")
    @Schema(description = "Password / Senha", example = "admin", required = true)
    private String password;

    @Schema(description = "Preferred language / Idioma preferido", example = "en-CA")
    private String language;

    @Schema(description = "Remember me option / Opção lembrar-me", example = "false")
    private Boolean rememberMe = false;
}

