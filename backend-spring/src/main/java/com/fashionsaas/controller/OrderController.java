package com.fashionsaas.controller;

import com.fashionsaas.dto.request.UpdateOrderStatusRequest;
import com.fashionsaas.dto.response.OrderResponse;
import com.fashionsaas.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador para la gestión de órdenes desde el panel admin.
 * Usa el Django token almacenado en el request para autenticar en Django.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Gestión de órdenes de la tienda")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    /**
     * Retorna la lista de órdenes de la tienda autenticada.
     *
     * @param request       petición HTTP con el Django token como atributo
     * @param status        filtro opcional por estado de orden
     * @param paymentStatus filtro opcional por estado de pago
     * @return lista de órdenes de la tienda
     */
    @GetMapping
    @Operation(summary = "Listar órdenes", description = "Retorna todas las órdenes de la tienda con filtros opcionales")
    public ResponseEntity<List<OrderResponse>> getOrders(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus
    ) {
        String djangoToken = (String) request.getAttribute("django_token");
        List<OrderResponse> orders = orderService.getStoreOrders(djangoToken, status, paymentStatus);
        return ResponseEntity.ok(orders);
    }

    /**
     * Retorna el detalle de una orden específica.
     *
     * @param request petición HTTP con el Django token como atributo
     * @param orderId identificador de la orden
     * @return detalle de la orden
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Detalle de orden", description = "Retorna el detalle de una orden específica")
    public ResponseEntity<OrderResponse> getOrderDetail(
            HttpServletRequest request,
            @PathVariable Long orderId
    ) {
        String djangoToken = (String) request.getAttribute("django_token");
        OrderResponse order = orderService.getOrderDetail(djangoToken, orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Actualiza el estado de una orden específica.
     *
     * @param request       petición HTTP con el Django token como atributo
     * @param orderId       identificador de la orden
     * @param statusRequest nuevo estado de la orden
     * @return orden con el estado actualizado
     */
    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de una orden")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            HttpServletRequest request,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest statusRequest
    ) {
        String djangoToken = (String) request.getAttribute("django_token");
        OrderResponse order = orderService.updateOrderStatus(djangoToken, orderId, statusRequest.getStatus());
        return ResponseEntity.ok(order);
    }
}