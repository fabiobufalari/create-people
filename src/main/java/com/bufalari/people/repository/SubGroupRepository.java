package com.bufalari.people.repository;

import com.bufalari.people.entity.GroupEntity;
import com.bufalari.people.entity.SubGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Repository for accessing SubGroup data (using UUID).
 * Repositório para acessar dados de SubGrupo (usando UUID).
 */
@Repository
public interface SubGroupRepository extends JpaRepository<SubGroupEntity, UUID> { // <<<--- Alterado para UUID

    /**
     * Finds all subgroups belonging to a specific parent group (by UUID).
     * Encontra todos os subgrupos pertencentes a um grupo pai específico (por UUID).
     * @param groupId The UUID of the parent group. / O UUID do grupo pai.
     * @return A list of subgroups. / Uma lista de subgrupos.
     */
    List<SubGroupEntity> findByGroup_Id(UUID groupId); // <<<--- Alterado para UUID

    /**
     * Finds a subgroup by its name within a specific parent group.
     * Encontra um subgrupo por seu nome dentro de um grupo pai específico.
     * @param name The name of the subgroup. / O nome do subgrupo.
     * @param group The parent GroupEntity (which has a UUID ID). / A entidade GroupEntity pai (que tem um ID UUID).
     * @return Optional containing the subgroup if found. / Optional contendo o subgrupo se encontrado.
     */
    Optional<SubGroupEntity> findByNameAndGroup(String name, GroupEntity group); // O parâmetro GroupEntity já contém o UUID

    /**
     * Checks if any subgroup belongs to the specified group (by UUID).
     * Useful before deleting a group.
     * Verifica se algum subgrupo pertence ao grupo especificado (por UUID).
     * Útil antes de deletar um grupo.
     * @param groupId The UUID of the parent group. / O UUID do grupo pai.
     * @return true if subgroups exist for the group, false otherwise.
     */
    boolean existsByGroup_Id(UUID groupId); // <<<--- Alterado para UUID

    // existsById(UUID id) e findById(UUID id) são herdados do JpaRepository
}