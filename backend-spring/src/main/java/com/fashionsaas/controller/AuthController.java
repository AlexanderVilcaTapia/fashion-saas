package com.fashionsaas.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import com.fashionsaas.dto.request.LoginRequest;
import com.fashionsaas.dto.response.AuthResponse;
import com.fashionsaas.security.jwt.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Controlador de autenticación para el panel admin.
 * Permite a los dueños de tienda iniciar sesión con JWT.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login para el panel admin")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate;

    @Value("${app.django.api.url}")
    private String djangoApiUrl;

  /**
     * Autentica al dueño de tienda contra la API de Django
     * y devuelve un token JWT para el panel admin.
     *
     * @param request credenciales de login (email y password)
     * @return token JWT y datos básicos del usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Login del dueño de tienda", description = "Autentica contra Django y devuelve un JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        try {
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            // Construir body como String JSON directamente
            String jsonBody = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    request.getEmail(),
                    request.getPassword()
            );

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // Llamar a Django
            org.springframework.http.ResponseEntity<Map> djangoResponseEntity = restTemplate.exchange(
                    djangoApiUrl + "/auth/login/",
                    org.springframework.http.HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> djangoTokens = djangoResponseEntity.getBody();
            if (djangoTokens == null) {
                return ResponseEntity.status(401).build();
            }

            // Con los tokens de Django, obtener datos del usuario
            String accessToken = (String) djangoTokens.get("access");
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            org.springframework.http.ResponseEntity<Map> userResponseEntity = restTemplate.exchange(
                    djangoApiUrl + "/auth/me/",
                    org.springframework.http.HttpMethod.GET,
                    userRequest,
                    Map.class
            );

            Map<String, Object> userData = userResponseEntity.getBody();
            if (userData == null) {
                return ResponseEntity.status(401).build();
            }

            String email = (String) userData.get("email");
            String fullName = (String) userData.get("full_name");
            String role = (String) userData.get("role");

            // Verificar que el usuario es dueño de tienda o admin
            if (!role.equals("store_owner") && !role.equals("admin")) {
                return ResponseEntity.status(403).build();
            }

            // Generar token JWT propio del panel admin
            String token = jwtUtils.generateToken(email, accessToken);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .email(email)
                    .fullName(fullName)
                    .role(role)
                    .build();

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}