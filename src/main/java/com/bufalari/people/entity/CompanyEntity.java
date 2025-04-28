package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "companies") // Define table name
public class CompanyEntity extends AuditableBaseEntity {

    @Id
    // If IDs are manually set in DataLoader, remove GeneratedValue or use appropriate strategy
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String city;

    // Add other relevant company fields if needed
}