package com.fashionsaas.controller;

import com.fashionsaas.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para la gestión de inventario de la tienda.
 * Usa el Django token almacenado en el request para autenticar en Django.
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
     *
     * @param request  petición HTTP con el Django token como atributo
     * @param status   filtro opcional por estado del producto
     * @param category filtro opcional por categoría
     * @param page     número de página
     * @return lista paginada de productos
     */
    @GetMapping("/products")
    @Operation(summary = "Listar productos", description = "Retorna los productos de la tienda con filtros opcionales")
    public ResponseEntity<Map<String, Object>> getProducts(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page
    ) {
        String djangoToken = (String) request.getAttribute("django_token");
        Map<String, Object> products = inventoryService.getStoreProducts(djangoToken, status, category, page);
        return ResponseEntity.ok(products);
    }

    /**
     * Retorna un resumen del inventario de la tienda.
     *
     * @param request petición HTTP con el Django token como atributo
     * @return resumen del inventario
     */
    @GetMapping("/summary")
    @Operation(summary = "Resumen de inventario", description = "Retorna un resumen del estado del inventario")
    public ResponseEntity<Map<String, Object>> getInventorySummary(HttpServletRequest request) {
        String djangoToken = (String) request.getAttribute("django_token");
        Map<String, Object> summary = inventoryService.getInventorySummary(djangoToken);
        return ResponseEntity.ok(summary);
    }
}