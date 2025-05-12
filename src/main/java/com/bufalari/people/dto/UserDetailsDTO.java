package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO local para representar os detalhes do usuário recebidos do serviço de autenticação.
 * Define a estrutura esperada pelo create-people-service ao chamar o AuthServiceClient.
 *
 * Local DTO to represent user details received from the authentication service.
 * Defines the structure expected by the create-people-service when calling AuthServiceClient.
 */
@Data // Lombok: getters, setters, equals, hashCode, toString
@NoArgsConstructor // Lombok: construtor sem argumentos
@AllArgsConstructor // Lombok: construtor com todos os argumentos
public class UserDetailsDTO {

    @Schema(description = "User's unique identifier (UUID)", example = "f0e9d8c7-b6a5-4321-fedc-ba9876543210")
    private UUID id; // ID do usuário (UUID)

    @Schema(description = "Username used for login", example = "jsilva")
    private String username; // Nome de usuário

    // A senha NUNCA deve ser trafegada ou armazenada aqui.
    // private String password; << NÃO INCLUIR

    @Schema(description = "List of roles assigned to the user", example = "[\"ADMIN\", \"USER\"]")
    private List<String> roles; // Lista de papéis/roles do usuário
}