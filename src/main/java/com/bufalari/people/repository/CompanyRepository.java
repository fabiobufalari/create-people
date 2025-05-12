package com.bufalari.people.repository;

import com.bufalari.people.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Repository interface for CompanyEntity (using UUID).
 * Interface de repositório para CompanyEntity (usando UUID).
 */
@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> { // <<<--- Alterado para UUID
    // Adicionar métodos de consulta personalizados se necessário
    // Add custom query methods if needed
    // Ex: Optional<CompanyEntity> findByName(String name);
}