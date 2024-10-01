package com.bufalari.dto;

import lombok.Data;
import java.util.List;

@Data
public class GeocodingResponseDTO {
    private List<GeocodingResultDTO> results;
    private String status;
}