package com.fashionsaas.controller;

import com.fashionsaas.dto.response.DashboardStatsResponse;
import com.fashionsaas.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador del dashboard del panel admin.
 * Muestra estadísticas generales de la tienda autenticada.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Estadísticas generales de la tienda")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retorna las estadísticas principales de la tienda:
     * ingresos totales, órdenes, productos y órdenes pendientes.
     *
     * @param userDetails usuario autenticado extraído del JWT
     * @return estadísticas del dashboard
     */
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas del dashboard", description = "Retorna ingresos, órdenes y productos de la tienda")
    public ResponseEntity<DashboardStatsResponse> getStats(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DashboardStatsResponse stats = dashboardService.getStoreStats(userDetails.getUsername());
        return ResponseEntity.ok(stats);
    }
}