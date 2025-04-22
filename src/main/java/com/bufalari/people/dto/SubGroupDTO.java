package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for subgroup of people.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubGroupDTO {

    @Schema(description = "Unique identifier of the subgroup", example = "1", readOnly = true)
    private Long id;

    @NotBlank(message = "Subgroup name cannot be blank")
    @Size(min = 2, max = 50, message = "Subgroup name must be between 2 and 50 characters")
    @Schema(description = "Name of the subgroup", example = "Cliente Pessoa Física", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Parent Group ID cannot be null")
    @Schema(description = "ID of the parent group this subgroup belongs to", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;

    @Schema(description = "Name of the parent group", example = "Clientes", readOnly = true)
    private String groupName; // Read-only, populated on retrieval
}
