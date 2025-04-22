package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO that represents a Group of people.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {

    @Schema(description = "Unique identifier of the group", example = "1", readOnly = true)
    private Long id;

    @NotBlank(message = "Group name cannot be blank")
    @Size(min = 2, max = 50, message = "Group name must be between 2 and 50 characters")
    @Schema(description = "Name of the group", example = "Clientes", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Group type cannot be blank")
    @Size(min = 3, max = 20, message = "Group type must be between 3 and 20 characters")
    @Schema(description = "Type or code for the group (e.g., CLIENT, EMPLOYEE)", example = "CLIENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type; // Added type field based on entity
}
