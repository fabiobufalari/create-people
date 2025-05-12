package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator; // Importar Gerador UUID
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Objects; // Importar Objects
import java.util.UUID; // <<<--- IMPORT UUID

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "people", indexes = { // Adicionar índices úteis
        @Index(name = "idx_person_document", columnList = "document", unique = true), // Índice único para documento
        @Index(name = "idx_person_email", columnList = "email", unique = true), // Índice único para email
        @Index(name = "idx_person_group_id", columnList = "group_id"),
        @Index(name = "idx_person_subgroup_id", columnList = "subgroup_id"),
        @Index(name = "idx_person_company_id", columnList = "company_id")
})
// Soft delete: ao deletar, executa este UPDATE. @Where filtra selects para não incluir deletados.
@SQLDelete(sql = "UPDATE people SET deleted = true, last_modified_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted = false")
// @EqualsAndHashCode(callSuper = true) // Cuidado com equals/hashCode
public class PersonEntity extends AuditableBaseEntity { // <<< Herda auditoria

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id; // <<<--- Alterado para UUID

    @Column(nullable = false, length = 100)
    private String fullName; // <<<--- Nome completo (corrigido do DTO 'name')

    @Column(nullable = false, unique = true, length = 50)
    private String document; // CPF, CNPJ, etc.

    @Column(unique = true, length = 100) // Email único
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 50)
    private String role;

    @Column
    private Integer age;

    // --- Endereço ---
    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province; // Estado/Província

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @Embedded // Embutir coordenadas geográficas
    private GeoCoordinatesEntity geoCoordinates;

    // --- Relacionamentos ---
    @ManyToOne(fetch = FetchType.LAZY) // Carregamento LAZY é melhor para performance
    @JoinColumn(name = "group_id", nullable = false, // FK não nula
            foreignKey = @ForeignKey(name = "fk_person_group")) // Nome da constraint (opcional)
    private GroupEntity group; // Referência para a entidade Group (com ID UUID)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id", // FK pode ser nula
            foreignKey = @ForeignKey(name = "fk_person_subgroup")) // Nome da constraint (opcional)
    private SubGroupEntity subGroup; // Referência para a entidade SubGroup (com ID UUID)

    // --- Company ID ---
    @Column(name = "company_id", nullable = false, columnDefinition = "uuid") // Mapeamento para UUID no DB
    private UUID companyId; // <<<--- Alterado para UUID (assumindo Company usa UUID)

    // --- Soft Delete Flag ---
    @Column(name = "deleted", nullable = false)
    @Builder.Default // Valor padrão no builder e no construtor default
    private boolean deleted = false;

    // Campos de Auditoria são herdados de AuditableBaseEntity

    // --- equals() e hashCode() baseados apenas no ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PersonEntity that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}