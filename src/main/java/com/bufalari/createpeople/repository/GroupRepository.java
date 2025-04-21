package com.bufalari.createpeople.repository;


import com.bufalari.createpeople.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para acesso à entidade Group.
 * Repository for accessing Group entity.
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    // Métodos de consulta adicionais podem ser definidos aqui, se necessário.
    // Additional query methods can be defined here if needed.
}
