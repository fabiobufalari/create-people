package com.bufalari.people.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * DTO for Person data transfer. Includes validation rules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDTO {

    @Schema(description = "Unique identifier of the person", example = "1", readOnly = true)
    private Long id;

    @NotBlank(message = "Person name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Full name of the person", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name; // Field name kept as 'name' for simplicity, maps to fullName in entity

    @NotBlank(message = "Document cannot be blank")
    // Add pattern validation if specific format (CPF, SIN) is expected
    // @Pattern(regexp = "^\\d{11}$", message = "Document must be a valid CPF (11 digits)")
    @Schema(description = "Unique document identifier (e.g., CPF, SIN)", example = "12345678900", requiredMode = Schema.RequiredMode.REQUIRED)
    private String document;

    @Email(message = "Invalid email format")
    @Schema(description = "Email address of the person", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Primary phone number", example = "+1-555-1234")
    private String phone;

    @Schema(description = "Role or position", example = "Project Manager")
    private String role;

    @Min(value = 0, message = "Age must be non-negative")
    @Schema(description = "Age of the person", example = "35")
    private Integer age;

    @Schema(description = "Street address", example = "123 Main St")
    private String address;

    @Schema(description = "City", example = "Halifax")
    private String city;

    @Schema(description = "Province or State", example = "Nova Scotia")
    private String province;

    @Schema(description = "Country", example = "Canada")
    private String country;

    @Schema(description = "Postal code or Zip code", example = "B3H 4R2")
    private String postalCode;

    @NotNull(message = "Company ID cannot be null") // Assuming person must belong to a company
    @Schema(description = "ID of the associated company", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long companyId;

    @NotNull(message = "Group ID cannot be null")
    @Schema(description = "ID of the main group the person belongs to", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;

    @Schema(description = "ID of the specific subgroup the person belongs to (optional)", example = "1")
    private Long subGroupId;

    // Read-only fields populated during conversion/retrieval
    @Schema(description = "Name of the associated group", example = "Funcionários", readOnly = true)
    private String groupName;

    @Schema(description = "Name of the associated subgroup", example = "Engenheiro Civil", readOnly = true)
    private String subGroupName;

    @Schema(description = "Links to view the address location on various map services", readOnly = true)
    private Map<String, String> mapLinks; // Changed from String to Map
}