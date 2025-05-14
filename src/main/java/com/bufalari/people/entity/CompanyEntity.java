package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate; // <<<--- IMPORTAR LocalDate
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "companies", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "uk_company_name") // Nome da empresa deve ser único
})
public class CompanyEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // <<<--- ESTRATÉGIA UUID
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String city;

    @Column(name = "foundation_date"/*, nullable = false*/) // A constraint NOT NULL está no banco
    private LocalDate foundationDate; // <<<--- ADICIONADO CAMPO

    // Adicionar outros campos relevantes da empresa se necessário
    // Ex: businessIdentificationNumber (CNPJ/EIN), address (como @Embedded ou relação)

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