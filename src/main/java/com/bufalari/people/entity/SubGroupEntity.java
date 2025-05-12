package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity; // Importar Base
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator; // Importar Gerador UUID

import java.util.Objects; // Importar Objects
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Entity representing a specific subgroup within a parent group (uses UUID ID).
 * Entidade representando um subgrupo específico dentro de um grupo pai (usa ID UUID).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subgroups", uniqueConstraints = {
        // Garante que o nome seja único DENTRO do mesmo grupo pai
        @UniqueConstraint(columnNames = {"name", "group_id"}, name = "uk_subgroup_name_group")
})
// @EqualsAndHashCode(callSuper = true) // Cuidado com equals/hashCode
public class SubGroupEntity extends AuditableBaseEntity { // <<< Herda Auditoria

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

    // Relação ManyToOne com Group: Muitos subgrupos pertencem a um grupo
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Subgrupo DEVE pertencer a um grupo
    @JoinColumn(name = "group_id", nullable = false, // Coluna FK não nula
            foreignKey = @ForeignKey(name = "fk_subgroup_group")) // Nome da constraint (opcional)
    private GroupEntity group; // Referência para GroupEntity (que tem ID UUID)

    // Relação com Pessoas: Um subgrupo tem muitas pessoas (opcional mapear aqui)
    // @OneToMany(mappedBy = "subGroup", fetch = FetchType.LAZY)
    // private List<PersonEntity> people = new ArrayList<>();

    // --- equals() e hashCode() baseados apenas no ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof SubGroupEntity that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}