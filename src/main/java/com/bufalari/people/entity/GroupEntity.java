package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a classification group for people.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "groups", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_group_name") // Explicit unique constraint name
})
public class GroupEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50) // Added length
    private String name;

    @Column(nullable = false, length = 20) // Added length
    private String type; // e.g., CLIENT, EMPLOYEE, SUPPLIER

    // If SubGroups should be deleted when a Group is deleted, cascade is appropriate.
    // orphanRemoval=true ensures that if a SubGroup is removed from this list (e.g., group.getSubGroups().remove(sg)), it gets deleted from DB.
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Initialize list for builder
    private List<SubGroupEntity> subGroups = new ArrayList<>();

    // Optional: Add convenience methods to manage subgroups bi-directionally if needed
    public void addSubGroup(SubGroupEntity subGroup) {
        subGroups.add(subGroup);
        subGroup.setGroup(this);
    }

    public void removeSubGroup(SubGroupEntity subGroup) {
        subGroups.remove(subGroup);
        subGroup.setGroup(null);
    }
}