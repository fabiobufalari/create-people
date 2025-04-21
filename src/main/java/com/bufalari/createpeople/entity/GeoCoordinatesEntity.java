package com.bufalari.createpeople.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

/**
 * Coordenadas geográficas (latitude e longitude).
 * Geographic coordinates (latitude and longitude).
 */
@Data
@Embeddable
public class GeoCoordinatesEntity {

    private Double latitude;
    private Double longitude;
}
