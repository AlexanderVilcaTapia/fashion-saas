package com.fashionsaas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para la actualización del perfil de una tienda.
 * Valida los campos antes de enviarlos a la API de Django.
 */
@Data
public class UpdateStoreRequest {

    /** Nombre de la tienda. */
    @NotBlank(message = "El nombre es requerido.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String name;

    /** Descripción de la tienda. */
    private String description;

    /** Dirección física de la tienda. */
    private String address;

    /** Ciudad donde se ubica la tienda. */
    private String city;

    /** Teléfono de contacto. */
    private String phone;

    /** Correo de contacto de la tienda. */
    @Email(message = "El email no es válido.")
    private String email;
}