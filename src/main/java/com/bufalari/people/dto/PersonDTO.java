package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * DTO for Person data transfer. Includes validation rules and uses UUIDs.
 * DTO para transferência de dados de Pessoa. Inclui regras de validação e usa UUIDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDTO {

    @Schema(description = "Unique identifier (UUID) of the person", example = "e1e2e3e4-f5f6-7890-1234-567890abcdef", readOnly = true)
    private UUID id; // <<<--- Changed to UUID

    @NotBlank(message = "{person.name.required}")
    @Size(min = 2, max = 100, message = "{person.name.size}")
    @Schema(description = "Full name of the person", example = "Maria Oliveira", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name; // Mapeia para 'fullName' na entidade

    @NotBlank(message = "{person.document.required}")
    // Adicionar @Pattern se houver formato específico (CPF, CNPJ, etc.)
    // @Pattern(regexp = "^\\d{11}$", message = "CPF inválido")
    // @Pattern(regexp = "^\\d{14}$", message = "CNPJ inválido")
    @Size(min = 5, max = 50, message = "{person.document.size}") // Tamanho flexível
    @Schema(description = "Unique document identifier (e.g., CPF, CNPJ, Passport)", example = "123.456.789-00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String document;

    @Email(message = "{person.email.invalid}")
    @Size(max = 100, message = "{person.email.size}")
    @Schema(description = "Email address of the person", example = "maria.oliveira@email.com", nullable = true)
    private String email; // Pode ser nulo

    @Size(max = 30, message = "{person.phone.size}")
    @Schema(description = "Primary phone number", example = "+55 (11) 99999-8888", nullable = true)
    private String phone; // Pode ser nulo

    @Size(max = 50, message = "{person.role.size}")
    @Schema(description = "Role or position", example = "Analista Financeiro", nullable = true)
    private String role; // Pode ser nulo

    @Min(value = 0, message = "{person.age.min}")
    @Max(value = 150, message = "{person.age.max}") // Limite razoável
    @Schema(description = "Age of the person", example = "35", nullable = true)
    private Integer age; // Pode ser nulo

    @Size(max = 255, message = "{person.address.size}")
    @Schema(description = "Street address", example = "Rua das Flores, 123", nullable = true)
    private String address; // Pode ser nulo

    @Size(max = 100, message = "{person.city.size}")
    @Schema(description = "City", example = "São Paulo", nullable = true)
    private String city; // Pode ser nulo

    @Size(max = 100, message = "{person.province.size}")
    @Schema(description = "Province or State", example = "SP", nullable = true)
    private String province; // Pode ser nulo

    @Size(max = 100, message = "{person.country.size}")
    @Schema(description = "Country", example = "Brasil", nullable = true)
    private String country; // Pode ser nulo

    @Size(max = 20, message = "{person.postalCode.size}")
    @Schema(description = "Postal code or Zip code", example = "01000-000", nullable = true)
    private String postalCode; // Pode ser nulo

    @NotNull(message = "{person.companyId.required}")
    @Schema(description = "UUID of the associated company", example = "00000000-0000-0000-0000-000000000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID companyId; // <<<--- Changed to UUID

    @NotNull(message = "{person.groupId.required}")
    @Schema(description = "UUID of the main group the person belongs to", example = "00000000-0000-0000-0000-000000000100", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID groupId; // <<<--- Changed to UUID

    @Schema(description = "UUID of the specific subgroup the person belongs to (optional)", example = "a0a0a0a0-b1b1-c2c2-d3d3-e4e4e4e4e4e4", nullable = true)
    private UUID subGroupId; // <<<--- Changed to UUID (Nullable)

    // Campos Read-only populados na conversão/recuperação
    @Schema(description = "Name of the associated group", example = "Clientes", readOnly = true)
    private String groupName;

    @Schema(description = "Name of the associated subgroup", example = "Cliente Pessoa Física", readOnly = true, nullable = true)
    private String subGroupName;

    @Schema(description = "Links to view the address location on various map services", readOnly = true, nullable = true)
    private Map<String, String> mapLinks; // Mantém Map<String, String>
}