package com.bufalari.createpeople.repository;

import com.bufalari.createpeople.dto.GeocodingResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "googleGeocodingClient", url = "${geocoding.api.url}")
public interface GeocodingClientRepository {

    @GetMapping
    GeocodingResponseDTO getCoordinates(@RequestParam("address") String address,
                                        @RequestParam("key") String apiKey);
}
