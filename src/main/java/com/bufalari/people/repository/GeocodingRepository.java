package com.bufalari.people.repository;

import com.bufalari.people.config.GeoProperties;
import com.bufalari.people.dto.GeocodingResponseDTO;
import feign.Request; // Correct import for Request.Options
import feign.Retryer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.TimeUnit; // Correct import for TimeUnit

/**
 * Feign client for interacting with the Geocoding API.
 * Includes configuration for timeouts and retries.
 */
@FeignClient(
        name = "geocodingClient", // Service name (can be arbitrary if URL is specified)
        url = "${geocoding.api.url}", // URL from properties
        configuration = GeocodingRepository.GeocodingFeignConfig.class // Reference inner config class
)
public interface GeocodingRepository {

    @GetMapping // Assuming the API endpoint is the base URL + query parameters
    GeocodingResponseDTO getCoordinates(
            @RequestParam("address") String address,
            @RequestParam("key") String apiKey
    );

    /**
     * Inner configuration class for this Feign client.
     * Defines beans specific to this client, like Retryer and Request.Options.
     */
    class GeocodingFeignConfig {

        // Define retry logic based on properties
        @Bean
        public Retryer retryer(GeoProperties props) {
            return new Retryer.Default(
                    props.getInitialInterval(), // period (initial wait time)
                    props.getMaxInterval(),     // maxPeriod (max wait time)
                    props.getMaxAttempts()      // maxAttempts (total attempts including first)
            );
        }

        // Define connection and read timeouts based on properties
        @Bean
        public Request.Options options(GeoProperties props) {
            return new Request.Options(
                    props.getTimeout(), // connectTimeoutMillis
                    TimeUnit.MILLISECONDS, // connectTimeoutUnit
                    props.getTimeout(), // readTimeoutMillis
                    TimeUnit.MILLISECONDS, // readTimeoutUnit
                    true // followRedirects
            );
        }
    }
}