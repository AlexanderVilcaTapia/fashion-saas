package com.fashionsaas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta con la información de una orden.
 * Se usa en el panel admin para que la tienda gestione sus órdenes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    /** Identificador único de la orden. */
    private Long id;

    /** Nombre completo del comprador. */
    private String buyerName;

    /** Nombre de la tienda. */
    private String storeName;

    /** Estado actual de la orden. */
    private String status;

    /** Estado del pago. */
    private String paymentStatus;

    /** Subtotal de la orden. */
    private Double subtotal;

    /** Costo de envío. */
    private Double shippingCost;

    /** Total de la orden. */
    private Double total;

    /** Dirección de envío. */
    private String shippingAddress;

    /** Ciudad de envío. */
    private String shippingCity;

    /** Teléfono de contacto para el envío. */
    private String shippingPhone;

    /** Fecha de creación de la orden. */
    private String createdAt;
}