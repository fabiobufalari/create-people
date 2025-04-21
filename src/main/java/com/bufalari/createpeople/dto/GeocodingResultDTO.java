package com.bufalari.createpeople.dto;

import lombok.Data;

/**
 * Resultado individual retornado pela API de geocodificação.
 * Individual result returned by the geocoding API.
 */
@Data
public class GeocodingResultDTO {
    private GeometryDTO geometry;
}
