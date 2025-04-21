package com.bufalari.createpeople.repository;


import com.bufalari.createpeople.entity.SubGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para acesso à entidade SubGroup.
 * Repository for accessing SubGroup entity.
 */
@Repository
public interface SubGroupRepository extends JpaRepository<SubGroupEntity, Long> {
    List<SubGroupEntity> findByGroup_Id(Long groupId);
}
