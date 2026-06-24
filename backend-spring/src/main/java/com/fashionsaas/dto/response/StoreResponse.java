package com.fashionsaas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con la información de una tienda.
 * Se usa para mostrar los datos de la tienda en el panel admin.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponse {

    /** Identificador único de la tienda. */
    private Long id;

    /** Nombre de la tienda. */
    private String name;

    /** Slug único de la tienda. */
    private String slug;

    /** Descripción de la tienda. */
    private String description;

    /** Dirección de la tienda. */
    private String address;

    /** Ciudad de la tienda. */
    private String city;

    /** Teléfono de la tienda. */
    private String phone;

    /** Correo de la tienda. */
    private String email;

    /** Estado actual de la tienda. */
    private String status;

    /** Total de productos registrados. */
    private Integer totalProducts;

    /** Nombre del dueño de la tienda. */
    private String ownerName;
}