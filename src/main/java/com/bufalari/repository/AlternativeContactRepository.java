package com.bufalari.repository;

import com.bufalari.entity.AlternativeContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlternativeContactRepository extends JpaRepository<AlternativeContactEntity, Long> {
}