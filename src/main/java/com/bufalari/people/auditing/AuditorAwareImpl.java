package com.bufalari.people.auditing; // Pacote correto

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
// import org.springframework.stereotype.Component; // REMOVA @Component

import java.util.Optional;

// NO @Component annotation here / SEM anotação @Component aqui
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // <<<--- AJUSTE AQUI ---<<<
            // Usuário padrão específico para este serviço
            return Optional.of("system_people"); // User do sistema para este serviço
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof User userPrincipal) { // Pattern matching
            username = userPrincipal.getUsername();
        } else if (principal instanceof String stringPrincipal) {
            username = stringPrincipal;
        } else {
            // Consider logging a warning here if principal is unexpected type
            return Optional.of("unknown_user"); // Usuário desconhecido
        }
        return Optional.of(username);
    }
}