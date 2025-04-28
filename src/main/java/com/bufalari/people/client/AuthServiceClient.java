package com.bufalari.people.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authentication-service", url = "${authentication.service.url}") // <<< NOTE a prop url aqui
public interface AuthServiceClient {
    @GetMapping("/api/auth/me") // <<< PATH ATUAL
    Object getUserDetails(@RequestHeader("Authorization") String authorizationHeader);
}