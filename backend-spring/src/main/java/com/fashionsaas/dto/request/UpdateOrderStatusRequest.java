package com.fashionsaas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para actualizar el estado de una orden desde el panel admin.
 * Solo permite valores de estado válidos.
 */
@Data
public class UpdateOrderStatusRequest {

    /** Nuevo estado de la orden. */
    @NotBlank(message = "El estado es requerido.")
    @Pattern(
        regexp = "confirmed|shipped|delivered|cancelled",
        message = "Estado inválido. Valores permitidos: confirmed, shipped, delivered, cancelled."
    )
    private String status;
}