package com.fashionsaas.controller;

import com.fashionsaas.dto.response.OrderResponse;
import com.fashionsaas.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión de inventario de la tienda.
 * Permite al dueño ver sus productos y filtrarlos por estado o categoría.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Gestión de productos e inventario de la tienda")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Retorna la lista de productos de la tienda autenticada.
     * Permite filtrar por estado y categoría.
     *
     * @param userDetails usuario autenticado extraído del JWT
     * @param status      filtro opcional por estado del producto
     * @param category    filtro opcional por categoría
     * @param page        número de página para paginación
     * @return lista paginada de productos de la tienda
     */
    @GetMapping("/products")
    @Operation(summary = "Listar productos", description = "Retorna los productos de la tienda con filtros opcionales")
    public ResponseEntity<Map<String, Object>> getProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page
    ) {
        Map<String, Object> products = inventoryService.getStoreProducts(
                userDetails.getUsername(),
                status,
                category,
                page
        );
        return ResponseEntity.ok(products);
    }

    /**
     * Retorna un resumen del inventario de la tienda:
     * total de productos, activos, inactivos y sin stock.
     *
     * @param userDetails usuario autenticado extraído del JWT
     * @return resumen del inventario
     */
    @GetMapping("/summary")
    @Operation(summary = "Resumen de inventario", description = "Retorna un resumen del estado del inventario")
    public ResponseEntity<Map<String, Object>> getInventorySummary(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Map<String, Object> summary = inventoryService.getInventorySummary(
                userDetails.getUsername()
        );
        return ResponseEntity.ok(summary);
    }
}