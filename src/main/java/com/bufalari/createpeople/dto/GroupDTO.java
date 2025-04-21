package com.bufalari.createpeople.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO que representa um Grupo de pessoas.
 * DTO that represents a Group of people.
 */
@Data
public class GroupDTO {

    private Long id;

    @NotBlank(message = "{group.name.notBlank}")
    private String name; // Nome do grupo / Group name
}
