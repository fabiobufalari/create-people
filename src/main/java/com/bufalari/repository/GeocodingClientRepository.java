package com.bufalari.repository;

import com.bufalari.dto.GeocodingResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "geocodingClient", url = "https://maps.googleapis.com/maps/api/geocode") // Corrigido
public interface GeocodingClientRepository { // Removido @Repository

    @GetMapping("/json")
    GeocodingResponseDTO getCoordinates(@RequestParam("address") String address,
                                        @RequestParam("key") String apiKey);

    static String getAccessToken() {
        return "AIzaSyA3jbxRsV1FSjXQYD3g5rYjvoKt9yI4ZkQ";
    }
}