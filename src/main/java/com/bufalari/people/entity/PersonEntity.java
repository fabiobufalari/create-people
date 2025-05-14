package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;
// import org.hibernate.annotations.GenericGenerator; // REMOVIDO
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where; // Pode ser necessário @SQLRestriction com Hibernate 6+

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "people", indexes = {
        @Index(name = "idx_person_document", columnList = "document", unique = true),
        @Index(name = "idx_person_email", columnList = "email", unique = true),
        @Index(name = "idx_person_group_id", columnList = "group_id"),
        @Index(name = "idx_person_subgroup_id", columnList = "subgroup_id"),
        @Index(name = "idx_person_company_id", columnList = "company_id")
})
@SQLDelete(sql = "UPDATE people SET deleted = true, last_modified_at = CURRENT_TIMESTAMP WHERE id = ?")
// Para Hibernate 6+, @Where foi depreciado em favor de @SQLRestriction
// Se usar Hibernate 6+, mudar para: @org.hibernate.annotations.SQLRestriction("deleted = false")
@Where(clause = "deleted = false")
public class PersonEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // <<<--- CORRIGIDO
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 50)
    private String document;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 50)
    private String role;

    @Column
    private Integer age;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @Embedded
    private GeoCoordinatesEntity geoCoordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_person_group"))
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id",
            foreignKey = @ForeignKey(name = "fk_person_subgroup"))
    private SubGroupEntity subGroup;

    @Column(name = "company_id", nullable = false, columnDefinition = "uuid")
    private UUID companyId;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonEntity that = (PersonEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}