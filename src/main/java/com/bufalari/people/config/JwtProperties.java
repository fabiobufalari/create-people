package com.bufalari.people.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT settings (primarily the secret key for validation).
 */
@Getter
@Setter
@Component // Torna um bean gerenciado pelo Spring
// --- Prefixo CORRIGIDO para corresponder ao application.yml ---
@ConfigurationProperties(prefix = "security.jwt.token")
public class JwtProperties {

    // O nome da variável DEVE corresponder à chave no YAML após o prefixo
    private String secretKey; // Mapeia security.jwt.token.secret-key

    // A expiração é geralmente validada pelo JwtUtil, não precisa ser propriedade aqui
    // private long expiration;
}