package com.bufalari.createpeople.repository;

import com.bufalari.createpeople.config.GeoProperties;
import com.bufalari.createpeople.dto.GeocodingResponseDTO;
import feign.Retryer; // Import adicionado
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "geocodingClient",
        url = "${geocoding.api.url}",
        configuration = GeocodingRepository.FeignConfig.class
)
public interface GeocodingRepository {

    @GetMapping
    GeocodingResponseDTO getCoordinates(
            @RequestParam("address") String address,
            @RequestParam("key") String apiKey
    );

    class FeignConfig {
        @Bean
        public Retryer retryer(GeoProperties props) {
            return new Retryer.Default(
                    props.getInitialInterval(), // ✅ Acesso correto
                    props.getMaxInterval(),     // ✅
                    props.getMaxAttempts()      // ✅
            );
        }
    }
}