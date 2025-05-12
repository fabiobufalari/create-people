package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity; // Importar Base
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator; // Importar Gerador UUID

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Importar Objects
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Entity representing a classification group for people (uses UUID ID).
 * Entidade representando um grupo de classificação para pessoas (usa ID UUID).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "groups", uniqueConstraints = {
        // Nome do grupo deve ser único
        @UniqueConstraint(columnNames = "name", name = "uk_group_name")
})
// @EqualsAndHashCode(callSuper = true) // Cuidado com equals/hashCode
public class GroupEntity extends AuditableBaseEntity { // <<< Herda Auditoria

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id; // <<<--- Alterado para UUID

    @Column(nullable = false, length = 50) // Nome não nulo, tamanho 50
    private String name;

    @Column(nullable = false, length = 20) // Tipo não nulo, tamanho 20
    private String type; // Ex: CLIENT, EMPLOYEE, SUPPLIER

    // Relação com Subgrupos: Um grupo tem muitos subgrupos
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Inicializa a lista
    private List<SubGroupEntity> subGroups = new ArrayList<>();

    // Relação com Pessoas: Um grupo tem muitas pessoas (opcional mapear aqui, já mapeado em PersonEntity)
    // @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    // private List<PersonEntity> people = new ArrayList<>();

    // --- Métodos auxiliares (opcional) ---
    public void addSubGroup(SubGroupEntity subGroup) {
        if (subGroup != null) {
            if (this.subGroups == null) {
                this.subGroups = new ArrayList<>();
            }
            subGroups.add(subGroup);
            subGroup.setGroup(this); // Mantém consistência bidirecional
        }
    }

    public void removeSubGroup(SubGroupEntity subGroup) {
        if (subGroup != null && this.subGroups != null) {
            subGroups.remove(subGroup);
            subGroup.setGroup(null); // Remove a referência
        }
    }

    // --- equals() e hashCode() baseados apenas no ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GroupEntity that)) return false; // Verifica tipo e faz cast seguro
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}