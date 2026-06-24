package com.fashionsaas.controller;

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

        // Autenticar contra Django API
        Map<String, String> djangoRequest = Map.of(
                "email", request.getEmail(),
                "password", request.getPassword()
        );

        Map<String, Object> djangoResponse = restTemplate.postForObject(
                djangoApiUrl + "/auth/login/",
                djangoRequest,
                Map.class
        );

        if (djangoResponse == null) {
            return ResponseEntity.status(401).build();
        }

        // Extraer datos del usuario de la respuesta de Django
        Map<String, Object> userData = (Map<String, Object>) djangoResponse.get("user");
        String email = (String) userData.get("email");
        String fullName = (String) userData.get("full_name");
        String role = (String) userData.get("role");

        // Verificar que el usuario es dueño de tienda
        if (!role.equals("store_owner") && !role.equals("admin")) {
            return ResponseEntity.status(403).build();
        }

        // Generar token JWT propio del panel admin
        String token = jwtUtils.generateToken(email);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();

        return ResponseEntity.ok(authResponse);
    }
}