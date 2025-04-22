package com.bufalari.people.repository;

import com.bufalari.people.entity.GroupEntity;
import com.bufalari.people.entity.SubGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing SubGroup data.
 */
@Repository
public interface SubGroupRepository extends JpaRepository<SubGroupEntity, Long> {

    /**
     * Finds all subgroups belonging to a specific parent group.
     * @param groupId The ID of the parent group.
     * @return A list of subgroups.
     */
    List<SubGroupEntity> findByGroup_Id(Long groupId);

    /**
     * Finds a subgroup by its name within a specific parent group.
     * @param name The name of the subgroup.
     * @param group The parent GroupEntity.
     * @return Optional containing the subgroup if found.
     */
    Optional<SubGroupEntity> findByNameAndGroup(String name, GroupEntity group);

    /**
     * Checks if any subgroup belongs to the specified group.
     * Useful before deleting a group.
     * @param groupId The ID of the parent group.
     * @return true if subgroups exist for the group, false otherwise.
     */
    boolean existsByGroup_Id(Long groupId);
}