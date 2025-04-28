package com.bufalari.people.entity;// Imports necessários, incluindo AuditableBaseEntity
import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter // Usar Getter/Setter individuais é ligeiramente preferível com herança
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "people")
@SQLDelete(sql = "UPDATE people SET deleted = true, updated_at = now() WHERE id = ?")
@Where(clause = "deleted = false")
// @EqualsAndHashCode(callSuper = true) // Adicionar se usar @Data na classe filho
public class PersonEntity extends AuditableBaseEntity { // <<< GARANTIR O EXTENDS

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 50)
    private String document;

    // ... outros campos específicos de PersonEntity ...

    @Embedded
    private GeoCoordinatesEntity geoCoordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id")
    private SubGroupEntity subGroup;

    // @CreationTimestamp // <<< REMOVER SE EXISTIR
    // @Column(nullable = false, updatable = false) // <<< REMOVER SE EXISTIR
    // private LocalDateTime createdAt; // <<< REMOVER SE EXISTIR

    // @UpdateTimestamp // <<< REMOVER SE EXISTIR
    // @Column(nullable = false) // <<< REMOVER SE EXISTIR
    // private LocalDateTime updatedAt; // <<< REMOVER SE EXISTIR

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

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

    @Column(name = "company_id", nullable = false) // Ajuste nullable se necessário
    private Long companyId;
}