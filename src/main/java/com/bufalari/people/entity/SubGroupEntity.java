package com.bufalari.people.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a specific subgroup within a parent group.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subgroups", uniqueConstraints = {
    // Ensure name is unique within the *same* group
    @UniqueConstraint(columnNames = {"name", "group_id"}, name = "uk_subgroup_name_group")
})
public class SubGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50) // Added length
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Subgroup must belong to a group
    @JoinColumn(name = "group_id", nullable = false) // Foreign key column
    private GroupEntity group;
}