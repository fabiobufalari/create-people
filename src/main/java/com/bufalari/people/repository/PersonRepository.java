package com.bufalari.people.repository;

import com.bufalari.people.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Importar @Param
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Repository for accessing Person data (using UUID). Includes check for document existence.
 * Repositório para acessar dados de Pessoa (usando UUID). Inclui checagem de existência de documento.
 */
@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, UUID> { // <<<--- Alterado para UUID

    /**
     * Checks if a non-deleted person exists with the given document.
     * The @Where annotation on the entity handles the 'deleted = false' clause automatically.
     * Verifica se uma pessoa não deletada existe com o documento fornecido.
     * A anotação @Where na entidade trata a cláusula 'deleted = false' automaticamente.
     * @param document The document to check. / O documento a ser verificado.
     * @return true if exists, false otherwise.
     */
    boolean existsByDocument(String document);

    /**
     * Optional: Explicit query to check existence by document, IGNORING the soft-delete status.
     * Use with caution.
     * Opcional: Consulta explícita para verificar existência por documento, IGNORANDO o status de soft-delete.
     * Use com cautela.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PersonEntity p WHERE p.document = :document")
    boolean existsByDocumentIncludingDeleted(@Param("document") String document);

    /**
     * Checks if any non-deleted person belongs to the specified group (by UUID).
     * The @Where annotation handles the 'deleted = false' clause.
     * Verifica se alguma pessoa não deletada pertence ao grupo especificado (por UUID).
     * A anotação @Where trata a cláusula 'deleted = false'.
     * @param groupId The UUID of the group. / O UUID do grupo.
     * @return true if exists, false otherwise.
     */
    boolean existsByGroup_Id(UUID groupId); // <<<--- Alterado para UUID

    /**
     * Checks if any non-deleted person belongs to the specified subgroup (by UUID).
     * The @Where annotation handles the 'deleted = false' clause.
     * Verifica se alguma pessoa não deletada pertence ao subgrupo especificado (por UUID).
     * A anotação @Where trata a cláusula 'deleted = false'.
     * @param subGroupId The UUID of the subgroup. / O UUID do subgrupo.
     * @return true if exists, false otherwise.
     */
    boolean existsBySubGroup_Id(UUID subGroupId); // <<<--- Alterado para UUID

    /**
     * Finds a person by ID, respecting the @Where(clause = "deleted = false").
     * Encontra uma pessoa por ID, respeitando @Where(clause = "deleted = false").
     * Método herdado, já funciona com UUID e @Where.
     */
    @Override
    Optional<PersonEntity> findById(UUID id); // <<<--- Herdado com UUID

    /**
     * Optional: If you NEED to find a deleted person by ID.
     * Opcional: Se você PRECISA encontrar uma pessoa deletada por ID.
     */
    @Query("SELECT p FROM PersonEntity p WHERE p.id = :id AND p.deleted = true")
    Optional<PersonEntity> findDeletedById(@Param("id") UUID id); // <<<--- Alterado para UUID
}