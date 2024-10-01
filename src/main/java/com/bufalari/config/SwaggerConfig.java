package com.bufalari.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Configuration class for Swagger API documentation.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the Docket bean for Swagger documentation.
     *
     * @return A Docket instance that scans the project for API endpoints and generates Swagger documentation.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30) // Use OAS_30 for OpenAPI 3.0
                .select()
                .build()
                .apiInfo(apiInfo());
    }

    /**
     * Provides API information for Swagger documentation.
     *
     * @return An ApiInfo object containing the API title, description, version, and contact information.
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Client Management API")
                .description("API for managing clients with contact information and geolocation.")
                .version("1.0.0")
                .contact(new Contact("Your Name", "Your Website", "your.email@example.com"))
                .build();
    }
}