package com.bufalari.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class GeoCoordinatesEntity { // Nome ajustado

    private Double latitude;
    private Double longitude;
}