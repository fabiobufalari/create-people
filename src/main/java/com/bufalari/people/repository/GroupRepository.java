package com.bufalari.people.repository;

import com.bufalari.people.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Repository for accessing Group data (using UUID).
 * Repositório para acessar dados de Grupo (usando UUID).
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, UUID> { // <<<--- Alterado para UUID

    /**
     * Finds a group by its unique name.
     * @param name The name of the group.
     * @return Optional containing the group if found.
     */
    Optional<GroupEntity> findByName(String name);

    /**
     * Checks if a group with the given name exists.
     * @param name The name of the group.
     * @return true if a group with that name exists, false otherwise.
     */
    boolean existsByName(String name);

    // existsById(UUID id) é herdado do JpaRepository
}