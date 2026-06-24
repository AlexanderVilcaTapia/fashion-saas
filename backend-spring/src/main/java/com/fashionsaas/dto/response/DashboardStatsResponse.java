package com.fashionsaas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta con las estadísticas del dashboard de la tienda.
 * Muestra un resumen de ventas, órdenes y productos.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {

    /** Ingresos totales de órdenes pagadas. */
    private Double totalRevenue;

    /** Total de órdenes recibidas. */
    private Integer totalOrders;

    /** Órdenes pendientes de confirmación. */
    private Integer pendingOrders;

    /** Total de productos registrados en la tienda. */
    private Integer totalProducts;
}