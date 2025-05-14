package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "companies", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "uk_company_name"),
    // Adicionar unique constraint para business_identification_number se ele deve ser único
    @UniqueConstraint(columnNames = "business_identification_number", name = "uk_company_business_id")
})
public class CompanyEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    // <<<--- ADICIONAR/VERIFICAR ESTE CAMPO ---<<<
    @Column(name = "business_identification_number", nullable = false, unique = true, length = 50)
    private String businessIdentificationNumber; // Ex: CNPJ, EIN, etc.

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String city;

    @Column(name = "foundation_date", nullable = false) // Já corrigido para ter nullable = false
    private LocalDate foundationDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyEntity that = (CompanyEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}