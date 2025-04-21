package com.bufalari;

import com.bufalari.createpeople.config.GeoProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Classe principal da aplicação Spring Boot para o microserviço create-people.
 * Main Spring Boot application class for the create-people microservice.
 */
@SpringBootApplication(scanBasePackages = "com.bufalari.createpeople")
@EnableFeignClients(basePackages = "com.bufalari.createpeople.repository")
@OpenAPIDefinition(
    info = @Info(
        title = "People API",
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
        in = SecuritySchemeIn.HEADER
    )
})
@EnableConfigurationProperties(GeoProperties.class)
public class CreatePeopleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreatePeopleApplication.class, args);
    }
}
