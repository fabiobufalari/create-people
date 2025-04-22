package com.bufalari.people.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Dummy UserDetailsService implementation.
 * In this microservice, it doesn't load users from a database.
 * It's used by Spring Security and JwtAuthFilter primarily to get a UserDetails object
 * based on the username extracted from the JWT, allowing the filter to validate the token signature/expiration.
 * The actual user authorization/roles might be handled by the Authentication Service or checked via claims in the token.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Since this service doesn't manage users directly, we return a dummy UserDetails object.
        // We assume the username extracted from a valid token corresponds to a real user
        // managed by the authentication service.
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be empty");
        }

        // Return a basic UserDetails object. The password is not used for JWT auth.
        // Authorities could potentially be extracted from the JWT claims in JwtAuthFilter
        // if needed, but here we return an empty list.
        return User.builder()
                .username(username)
                .password("") // Password is not used/checked here
                .authorities(Collections.emptyList()) // No authorities loaded locally
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}