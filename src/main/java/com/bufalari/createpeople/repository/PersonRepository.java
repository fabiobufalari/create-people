package com.bufalari.createpeople.repository;


import com.bufalari.createpeople.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para acesso à entidade Person.
 * Repository for accessing Person entity.
 */
@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    boolean existsByDocument(String document);
    boolean existsByGroup_Id(Long groupId);
    boolean existsBySubGroup_Id(Long subGroupId);
}
