package com.fashionsaas.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.fashionsaas.service.SalesReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para reportes de ventas de la tienda.
 * Permite al dueño consultar ventas por rango de fechas.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Reportes de ventas por rango de fechas")
@SecurityRequirement(name = "bearerAuth")
public class SalesReportController {

    private final SalesReportService salesReportService;

    /**
     * Retorna el reporte de ventas de la tienda en un rango de fechas.
     *
     * @param request   petición HTTP con el Django token como atributo
     * @param startDate fecha de inicio en formato YYYY-MM-DD
     * @param endDate   fecha de fin en formato YYYY-MM-DD
     * @return reporte de ventas del período indicado
     */
    @GetMapping("/sales")
    @Operation(summary = "Reporte de ventas", description = "Retorna ventas e ingresos en un rango de fechas")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        String djangoToken = (String) request.getAttribute("django_token");
        Map<String, Object> report = salesReportService.getSalesReport(djangoToken, startDate, endDate);
        return ResponseEntity.ok(report);
    }
}