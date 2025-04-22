package com.bufalari.people.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client to consume the authentication service.
 */
// Ensure 'authentication-service' name resolves correctly (e.g., via Eureka or Consul)
// OR provide a direct URL like url = "${authentication.service.url}"
@FeignClient(name = "authentication-service", url = "${authentication.service.url}")
public interface AuthServiceClient {

    /**
     * Retrieves authenticated user details based on the JWT token.
     *
     * @param authorizationHeader The authorization header containing the JWT token (e.g., "Bearer ...").
     * @return An Object, expected to be a Map<String, Object> with user details.
     */
    @GetMapping("/api/auth/me") // Verify this endpoint exists and returns expected details in the auth service
    Object getUserDetails(@RequestHeader("Authorization") String authorizationHeader);
}