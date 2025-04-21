package com.bufalari.createpeople.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um grupo de classificação de pessoas.
 * Entity that represents a group for classifying people.
 */
@Data
@AllArgsConstructor
@Builder
@Entity
@Table(name = "groups")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, unique = true)
    private String name; // Nome do grupo (ex: Cliente, Funcionário) / Group name (e.g., Client, Employee)

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubGroupEntity> subGroups = new ArrayList<>(); // Subgrupos pertencentes a este grupo / Subgroups belonging to this group

    public GroupEntity(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public GroupEntity() {}
}
