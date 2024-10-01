package com.bufalari.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ClientDTO {
    private Long id;

    @NotNull(message = "Client name cannot be null")
    @Size(min = 2, max = 100, message = "Client name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "City is mandatory")
    private String city;

    @NotNull(message = "Country is mandatory")
    private String country;

    @NotNull(message = "Province is mandatory")
    private String province;

    @NotNull(message = "Postal code is mandatory")
    private String postalCode;

    @NotNull(message = "Address is mandatory")
    private String address;

    @NotNull(message = "ddI is mandatory")
    private String ddI1;

    @NotNull(message = "Phone number 1 is mandatory")
    private String phoneNumber1;

    private String ddI2;

    private String phoneNumber2;

    @Email(message = "Invalid email format")
    @NotNull(message = "Email is mandatory")
    private String email;

    @NotNull(message = "SIN number is mandatory")
    private String sinNumber;

    private String notes;

    @NotEmpty(message = "At least one alternative contact is required")
    @Valid
    private List<AlternativeContactDTO> alternativeContacts;
}