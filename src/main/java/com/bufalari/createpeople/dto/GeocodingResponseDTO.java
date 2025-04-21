package com.bufalari.createpeople.dto;

import lombok.Data;

import java.util.List;

@Data
public class GeocodingResponseDTO {
    private List<Result> results;
    private String status;

    @Data
    public static class Result {
        private Geometry geometry;
        private String formatted_address;
    }

    @Data
    public static class Geometry {
        private Location location;
    }

    @Data
    public static class Location {
        private double lat;
        private double lng;
    }
}
