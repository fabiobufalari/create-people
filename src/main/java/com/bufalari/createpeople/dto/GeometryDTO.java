package com.bufalari.createpeople.dto;

import lombok.Data;

/**
 * Representa os dados de geometria recebidos do serviço de geocodificação.
 * Represents geometry data from geocoding service.
 */
@Data
public class GeometryDTO {
    private LocationDTO location;
}
