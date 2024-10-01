package com.bufalari.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ClientResponseDTO {
    private Long id;
    private String name;
    private String city;
    private String province;
    private String postalCode;
    private String address;
    private String ddI1; // Corrected field name
    private String phoneNumber1;
    private String ddI2; // Corrected field name
    private String phoneNumber2;
    private String email;
    private String country; // Added missing field
    private String notes; // Added missing field
    private Map<String, String> mapLink;
    private List<AlternativeContactDTO> alternativeContacts;
}