package com.bufalari.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlternativeContactDTO {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull(message = "DDI is mandatory")
    private String ddI;

    @NotBlank(message = "Phone number is mandatory")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    private String notes;

    private Long Id;
}