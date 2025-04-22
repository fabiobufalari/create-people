package com.bufalari.people.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete; // For logical delete
import org.hibernate.annotations.Where;   // For filtering deleted records

import java.time.LocalDateTime;

/**
 * Entity representing a person in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "people")
@SQLDelete(sql = "UPDATE people SET deleted = true, updated_at = now() WHERE id = ?") // Override delete to update flag
@Where(clause = "deleted = false") // Ensure find methods only return non-deleted records
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100) // Added length
    private String fullName; // Renamed from name for clarity

    @Column(nullable = false, unique = true, length = 50) // Added length
    private String document;

    @Column(length = 100) // Added length
    private String email;

    @Column(length = 30) // Added length
    private String phone;

    @Column(length = 50) // Added length
    private String role;

    @Column
    private Integer age;

    @Column(length = 255) // Added length
    private String address;

    @Column(length = 100) // Added length
    private String city;

    @Column(length = 100) // Added length
    private String province;

    @Column(length = 100) // Added length
    private String country;

    @Column(length = 20) // Added length
    private String postalCode;

    @Column(name = "company_id", nullable = false) // Explicit column name
    private Long companyId; // Keep as Long, no direct entity relation needed here unless required

    @Embedded // Embeddable for coordinates
    private GeoCoordinatesEntity geoCoordinates;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is generally better for performance
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subgroup_id") // Can be null if person doesn't belong to a subgroup
    private SubGroupEntity subGroup;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false; // Logical deletion flag
}