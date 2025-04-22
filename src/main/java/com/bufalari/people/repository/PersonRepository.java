package com.bufalari.people.repository;

import com.bufalari.people.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing Person data. Includes check for document existence.
 */
@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    /**
     * Checks if a person exists with the given document, ignoring logically deleted ones.
     * The @Where annotation on the entity should handle this automatically for JpaRepository methods.
     * This explicit query is an alternative or for cases where @Where might not apply.
     */
    boolean existsByDocument(String document); // @Where should filter deleted=true

    // Optional: Explicit query if you need to find even deleted ones by document
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PersonEntity p WHERE p.document = :document")
    boolean existsByDocumentIncludingDeleted(String document);

    /**
     * Checks if any non-deleted person belongs to the specified group.
     */
    boolean existsByGroup_Id(Long groupId); // @Where applies

    /**
     * Checks if any non-deleted person belongs to the specified subgroup.
     */
    boolean existsBySubGroup_Id(Long subGroupId); // @Where applies

     /**
      * Finds a person by ID, respecting the @Where(clause = "deleted = false")
      */
     @Override
     Optional<PersonEntity> findById(Long id); // Standard findById respects @Where

     // If you NEED to find a deleted person by ID:
     @Query("SELECT p FROM PersonEntity p WHERE p.id = :id AND p.deleted = true")
     Optional<PersonEntity> findDeletedById(Long id);
}