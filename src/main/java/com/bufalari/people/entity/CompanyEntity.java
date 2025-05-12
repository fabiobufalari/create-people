package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity; // Importar Base
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator; // Importar Gerador UUID

import java.util.Objects; // Importar Objects
import java.util.UUID; // <<<--- IMPORT UUID

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Adicionar Builder
@Entity
@Table(name = "companies") // Define o nome da tabela (geralmente plural)
// @EqualsAndHashCode(callSuper = true) // Cuidado com equals/hashCode em entidades JPA
public class CompanyEntity extends AuditableBaseEntity { // <<< Herda Auditoria

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid") // Mapeamento para UUID no DB
    private UUID id; // <<<--- Alterado para UUID

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String province; // Estado ou Província

    @Column(length = 100)
    private String city;

    // Adicionar outros campos relevantes da empresa se necessário
    // Ex: businessIdentificationNumber (CNPJ/EIN), address (como @Embedded ou relação)

    // --- equals() e hashCode() baseados apenas no ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Verifica se o objeto é nulo ou se a classe é diferente OU
        // se o objeto não é uma instância de CompanyEntity (necessário para proxies do Hibernate)
        if (o == null || !(o instanceof CompanyEntity that)) return false;
        // Só compara pelo ID se ambos não forem nulos
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // Usa o hash do ID se não for nulo, senão usa um valor fixo baseado na classe
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }
}