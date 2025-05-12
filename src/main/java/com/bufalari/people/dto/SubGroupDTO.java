package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID; // <<<--- IMPORT UUID

/**
 * DTO for subgroup of people (using UUID).
 * DTO para subgrupo de pessoas (usando UUID).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubGroupDTO {

    @Schema(description = "Unique identifier (UUID) of the subgroup", example = "a0a0a0a0-b1b1-c2c2-d3d3-e4e4e4e4e4e4", readOnly = true)
    private UUID id; // <<<--- Changed to UUID

    @NotBlank(message = "{subgroup.name.required}")
    @Size(min = 2, max = 50, message = "{subgroup.name.size}")
    @Schema(description = "Name of the subgroup", example = "Cliente Pessoa Física", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "{subgroup.groupId.required}")
    @Schema(description = "UUID of the parent group this subgroup belongs to", example = "c1c2c3c4-d5d6-e7e8-f9f0-a1a2a3a4a5a6", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID groupId; // <<<--- Changed to UUID

    @Schema(description = "Name of the parent group", example = "Clientes", readOnly = true)
    private String groupName; // Read-only, populated on retrieval
}