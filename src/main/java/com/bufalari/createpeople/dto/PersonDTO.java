package com.bufalari.createpeople.dto;

import lombok.Data;

/**
 * DTO principal da pessoa.
 * Main DTO for the person.
 */
@Data
public class PersonDTO {
    private Long id;
    private String name;
    private String document;
    private String email;
    private String phone;
    private String role;
    private Integer age;
    private String address;
    private String city;
    private String province;
    private String country;
    private String postalCode;
    private Long companyId;
    private Long groupId;
    private Long subGroupId;

    private String groupName;    // Nome do grupo associado / Group name
    private String subGroupName; // Nome do subgrupo associado / Subgroup name
    private String mapLink;      // Link para visualização no mapa / Link to view in maps
}
