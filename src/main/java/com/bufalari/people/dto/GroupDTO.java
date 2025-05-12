package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID; // <<<--- IMPORT UUID

/**
 * DTO that represents a Group of people (using UUID).
 * DTO que representa um Grupo de pessoas (usando UUID).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {

    @Schema(description = "Unique identifier (UUID) of the group", example = "c1c2c3c4-d5d6-e7e8-f9f0-a1a2a3a4a5a6", readOnly = true)
    private UUID id; // <<<--- Changed to UUID

    @NotBlank(message = "{group.name.required}") // Mensagem pode vir do messages.properties
    @Size(min = 2, max = 50, message = "{group.name.size}")
    @Schema(description = "Name of the group", example = "Clientes", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "{group.type.required}")
    @Size(min = 3, max = 20, message = "{group.type.size}")
    @Schema(description = "Type or code for the group (e.g., CLIENT, EMPLOYEE)", example = "CLIENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type; // Added type field based on entity
}