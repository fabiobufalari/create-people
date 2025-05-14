package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.*;
// import org.hibernate.annotations.GenericGenerator; // REMOVIDO

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "groups", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_group_name")
})
public class GroupEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // <<<--- CORRIGIDO
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String type;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubGroupEntity> subGroups = new ArrayList<>();

    public void addSubGroup(SubGroupEntity subGroup) {
        if (subGroup != null) {
            if (this.subGroups == null) {
                this.subGroups = new ArrayList<>();
            }
            subGroups.add(subGroup);
            subGroup.setGroup(this);
        }
    }

    public void removeSubGroup(SubGroupEntity subGroup) {
        if (subGroup != null && this.subGroups != null) {
            subGroups.remove(subGroup);
            subGroup.setGroup(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupEntity that = (GroupEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}