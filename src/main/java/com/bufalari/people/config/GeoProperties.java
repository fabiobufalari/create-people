package com.bufalari.people.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component; // Use @Component or define as @Bean

/**
 * Configuration properties for the geocoding service (e.g., Google Geocoding API).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "geocoding.api")
public class GeoProperties {

    // Base URL for the geocoding API
    private String url;

    // API Key for the geocoding service
    private String key;

    // Connection and read timeout in milliseconds
    private int timeout = 5000; // Default timeout 5 seconds

    // Feign Retryer settings
    private int retries = 3; // Default number of retries
    private long initialInterval = 100L; // Default initial interval (ms) for retry
    private long maxInterval = 1000L; // Default max interval (ms) for retry
    private int maxAttempts = 4; // Default max attempts (1 initial + 3 retries)

    // No static key needed, rely on injected instance
}