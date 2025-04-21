package com.bufalari.createpeople.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidade que representa um subgrupo de classificação de pessoas.
 * Entity that represents a subgroup for classifying people.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subgroups")
public class SubGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Nome do subgrupo / Subgroup name

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group; // Grupo pai deste subgrupo / Parent group of this subgroup
}
