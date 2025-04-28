package com.bufalari.people.entity;

import com.bufalari.people.auditing.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Embeddable for geographic coordinates (latitude and longitude).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable // Mark as embeddable
public class GeoCoordinatesEntity {

    @Column(name = "latitude") // Optional: Explicit column names
    private Double latitude;

    @Column(name = "longitude") // Optional: Explicit column names
    private Double longitude;
}