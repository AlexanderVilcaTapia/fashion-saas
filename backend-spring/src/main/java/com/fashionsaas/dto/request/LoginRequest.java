package com.fashionsaas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la petición de inicio de sesión.
 * Contiene las credenciales del dueño de tienda.
 */
@Data
public class LoginRequest {

    /** Correo electrónico del usuario. */
    @NotBlank(message = "El email es requerido.")
    @Email(message = "El email no es válido.")
    private String email;

    /** Contraseña del usuario. */
    @NotBlank(message = "La contraseña es requerida.")
    private String password;
}