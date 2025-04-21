package com.bufalari.createpeople.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma pessoa genérica no sistema.
 * Entity that represents a generic person in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "people")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName; // Nome completo da pessoa / Person's full name

    @Column(nullable = false, unique = true)
    private String document; // CPF, SIN ou outro identificador único / CPF, SIN or other unique identifier

    @Column
    private String email; // Email da pessoa / Person's email

    @Column
    private String phone; // Telefone principal / Primary phone number

    @Column
    private String role; // Função ou cargo (ex: Gerente, Cliente) / Role or position (e.g., Manager, Client)

    @Column
    private Integer age; // Idade da pessoa / Person's age

    @Column
    private String address; // Endereço / Address

    @Column
    private String city; // Cidade / City

    @Column
    private String province; // Estado ou Província / State or Province

    @Column
    private String country; // País / Country

    @Column
    private String postalCode; // Código postal / Postal code

    @Column
    private Long companyId; // ID da empresa associada / Associated company ID

    @Embedded
    private GeoCoordinatesEntity geoCoordinates; // Coordenadas geográficas / Geographic coordinates

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group; // Grupo de pessoa (categoria principal) / Person's group (main category)

    @ManyToOne
    @JoinColumn(name = "subgroup_id")
    private SubGroupEntity subGroup; // Subgrupo de pessoa (categoria específica) / Person's subgroup (specific category)

    @CreationTimestamp
    private LocalDateTime createdAt; // Data de criação / Creation timestamp

    @UpdateTimestamp
    private LocalDateTime updatedAt; // Data de atualização / Update timestamp

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted = false; // Indicador de exclusão lógica / Logical deletion flag
}
