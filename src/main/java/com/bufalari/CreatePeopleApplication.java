package com.bufalari;

import com.bufalari.people.config.GeoProperties;
import com.bufalari.people.config.JwtProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
// ... outros imports
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Spring Boot application class for the create-people microservice.
 */
@SpringBootApplication(scanBasePackages = "com.bufalari.people")
// --- AJUSTE AQUI ---
// Incluir AMBOS os pacotes que contêm interfaces @FeignClient
@EnableFeignClients(basePackages = {
        "com.bufalari.people.client", // Pacote do AuthServiceClient
        "com.bufalari.people.repository" // Pacote do GeocodingRepository
})
// Alternativa (mais simples se todos estiverem sob .people):
// @EnableFeignClients(basePackages = "com.bufalari.people")
// --- FIM DO AJUSTE ---
@OpenAPIDefinition(
        info = @Info(
                title = "People API (Create People Service)",
                version = "1.0",
                description = "API para gerenciamento de Pessoas / API for People management"
        ),
        security = { @SecurityRequirement(name = "bearerAuth") },
        servers = { @Server(url = "/", description = "Default Server") }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                in = SecuritySchemeIn.HEADER,
                description = "JWT Authorization header using the Bearer scheme. Example: 'Authorization: Bearer {token}'"
        )
})
@EnableConfigurationProperties({GeoProperties.class, JwtProperties.class})
public class CreatePeopleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreatePeopleApplication.class, args);
    }
}