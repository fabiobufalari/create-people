package com.bufalari.people.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Add logging
import org.slf4j.LoggerFactory; // Add logging
import org.springframework.lang.NonNull; // Use @NonNull for clarity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class); // Logger

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Dummy UserDetailsService

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, // Mark parameters as NonNull
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // If no Authorization header or not starting with Bearer, pass through
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // Extract token ("Bearer ".length = 7)

        try {
            username = jwtService.extractUsername(jwt); // Extract username from token

            // If username exists and there's no authentication in the current security context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details (using the dummy service in this microservice)
                // This primarily checks if the username format is valid/expected
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validate token (checks signature and expiration against the loaded user details)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, // Principal (can be username string or UserDetails object)
                                    null,        // Credentials (not needed for JWT)
                                    userDetails.getAuthorities() // Authorities
                            );
                    // Set details (like IP address, session ID) from the request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authentication in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT token validated successfully for user: {}", username);
                } else {
                     log.warn("JWT token validation failed for user: {}", username);
                }
            }
        } catch (Exception e) {
            // Log exceptions during token processing (e.g., expired, malformed)
            log.error("Error processing JWT token: {}", e.getMessage());
            // Optionally clear the security context if an error occurs
            // SecurityContextHolder.clearContext();
            // Depending on requirements, you might want to send an error response here,
            // but typically the filter chain continues and subsequent security checks handle it.
        }


        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}