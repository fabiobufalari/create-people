package com.bufalari.people.client;

import com.bufalari.people.dto.UserDetailsDTO; // Import DTO local
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader; // Importar RequestHeader

/**
 * Feign client for interacting with the Authentication Service.
 * Used primarily to fetch user details based on the JWT token for logging/auditing purposes.
 */
@FeignClient(name = "auth-service-client-people", url = "${authentication.service.url}")
public interface AuthServiceClient {

    /**
     * Fetches minimal user details based on the provided Authorization header (Bearer token).
     * Assumes the auth service has an endpoint like '/api/auth/me' that validates the token
     * and returns user details (excluding sensitive info like password).
     *
     * @param authorizationHeader The full Authorization header value (e.g., "Bearer eyJhbGci...")
     * @return UserDetailsDTO containing user information.
     */
    // --- GARANTIR QUE ESTE MÉTODO E PATH EXISTEM E ESTÃO CORRETOS ---
    @GetMapping("/api/auth/me") // Exemplo de path no auth-service que retorna o usuário do token
    UserDetailsDTO getUserDetailsFromToken(@RequestHeader("Authorization") String authorizationHeader);

    /**
     * Optional: Method to get user details by username (if needed elsewhere).
     */
    @GetMapping("/api/auth/users/username/{username}") // Exemplo de path no auth-service
    UserDetailsDTO getUserByUsername(@PathVariable("username") String username);

    /**
     * Optional: Method to get user details by ID (if needed elsewhere).
     */
    @GetMapping("/api/auth/users/{id}") // Exemplo de path no auth-service (passa UUID como string)
    UserDetailsDTO getUserById(@PathVariable("id") String userId);
}