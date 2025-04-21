package com.bufalari.createpeople.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // 🔐 Chave secreta definida no application.yaml
    // 🔐 Secret key defined in application.yaml
    @Value("${jwt.secret}")
    private String secretKey;

    // 🔑 Extrai o username (subject) do token JWT
    // 🔑 Extracts the username (subject) from the JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 🔍 Verifica se o token é válido para o usuário
    // 🔍 Checks if the token is valid for the given user
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ⛔ Verifica se o token está expirado
    // ⛔ Checks if the token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 📅 Extrai a data de expiração do token
    // 📅 Extracts expiration date from token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 🔍 Extrai um claim específico usando função
    // 🔍 Extracts a specific claim using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 📦 Extrai todos os claims do token
    // 📦 Extracts all claims from token
    protected Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 🧾 Converte a chave secreta (string Base64) para chave criptográfica
    // 🧾 Converts the Base64 secret string to a crypto key
    /*private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }*/
    private Key getSignInKey() {
        // Alterado: Usar UTF-8 em vez de Base64
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
