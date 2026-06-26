package com.fashionsaas.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para generación y validación de tokens JWT.
 * El token incluye el Django access token para usarlo
 * en llamadas posteriores a la API de Django.
 */
@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Genera un token JWT que incluye el email y el token de Django.
     *
     * @param email       correo del usuario autenticado
     * @param djangoToken token de acceso de Django
     * @return token JWT firmado
     */
    public String generateToken(String email, String djangoToken) {
        return Jwts.builder()
                .subject(email)
                .claim("django_token", djangoToken)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * Genera un token JWT solo con el email (sin Django token).
     *
     * @param email correo del usuario autenticado
     * @return token JWT firmado
     */
    public String generateToken(String email) {
        return generateToken(email, "");
    }

    /**
     * Extrae el email del token JWT.
     *
     * @param token token JWT
     * @return email del usuario
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el token de Django del token JWT.
     *
     * @param token token JWT
     * @return token de Django o cadena vacía si no existe
     */
    public String getDjangoTokenFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("django_token", String.class);
    }

    /**
     * Valida si el token JWT es válido y no ha expirado.
     *
     * @param token token JWT
     * @return true si es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene los claims del token JWT.
     *
     * @param token token JWT
     * @return claims del token
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Genera la clave secreta para firmar el token.
     *
     * @return clave secreta
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}