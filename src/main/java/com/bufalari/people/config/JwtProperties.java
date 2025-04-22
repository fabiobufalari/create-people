package com.bufalari.people.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component; // Use @Component or @Configuration

/**
 * Configuration properties for JWT settings.
 */
@Getter
@Setter
@Component // Make it a Spring bean
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    // private long expiration; // Consider adding expiration if needed here, though validation usually happens with the key

}