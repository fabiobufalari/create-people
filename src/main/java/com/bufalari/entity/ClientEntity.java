package com.bufalari.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "clients")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;

    private String country;

    private String province;

    private String postalCode;

    @Column(nullable = false)
    private String address;

    @Column(name = "phone_number1", nullable = false)
    private String phoneNumber1;

    private String ddI1;

    private String phoneNumber2;

    private String ddI2;

    @Column(nullable = false)
    private String email;

    private String sinNumber;

    private String notes;

    @Embedded
    private GeoCoordinatesEntity geoCoordinates;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private boolean deleted = false;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlternativeContactEntity> alternativeContacts = new ArrayList<>(); // Lista de contatos alternativos
}
