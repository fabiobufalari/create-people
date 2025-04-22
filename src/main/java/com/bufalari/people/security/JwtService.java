package com.bufalari.people.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm; // Needed if generating tokens
import io.jsonwebtoken.io.Decoders; // Needed for Base64 decoding if secret is Base64
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // For initializing key
import java.nio.charset.StandardCharsets; // For UTF-8 encoding
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKeyString; // Renamed to avoid confusion with Key type

    private Key signInKey; // Store the initialized key

    // Initialize the key once after properties are set
    @PostConstruct
    public void init() {
        // Assume the secret is a sufficiently long string, NOT Base64 encoded
        // For HS256, the key length should be at least 256 bits (32 bytes/chars).
        // If your secret is shorter, this might be insecure.
        // If your secret IS Base64 encoded, use:
        // byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        // this.signInKey = Keys.hmacShaKeyFor(keyBytes);
        this.signInKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // Extract username (subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Check if token is valid for the user
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Check username match and token expiration
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Check token expiration
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract a specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims (protected/private as it uses the key directly)
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) // Use the getter method
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Getter for the initialized sign-in key
    private Key getSignInKey() {
        return signInKey;
    }

    // --- Token Generation Methods (If this service were also creating tokens) ---
    /*
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Example: 10 hours
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    */
}