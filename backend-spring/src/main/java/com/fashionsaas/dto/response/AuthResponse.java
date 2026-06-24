package com.fashionsaas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la autenticación exitosa.
 * Contiene el token JWT y la información básica del usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    /** Token JWT generado para el usuario autenticado. */
    private String token;

    /** Correo electrónico del usuario autenticado. */
    private String email;

    /** Nombre completo del usuario autenticado. */
    private String fullName;

    /** Rol del usuario en el sistema. */
    private String role;
}