package com.bufalari.createpeople.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração para o serviço de geocodificação.
 * Configuration properties for geocoding service.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "geocoding.api")
public class GeoProperties {

    private static String staticKey;

    private String url;
    private String key;
    private int timeout;
    private int retries;
    private int initialInterval;
    private int maxInterval;
    private int maxAttempts;

    public void setKey(String key) {
        this.key = key;
        GeoProperties.staticKey = key;
    }

    public static String getStaticKey() {
        return staticKey;
    }
}
