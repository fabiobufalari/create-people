package com.bufalari.repository;

import com.bufalari.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByEmailAndSinNumberAndDeletedFalse(String email, String sinNumber);

    Optional<ClientEntity> findBySinNumberAndDeletedFalse(String sinNumber);

    List<ClientEntity> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    List<ClientEntity> findAllByDeletedFalse();
}