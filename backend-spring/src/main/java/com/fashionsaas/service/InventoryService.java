package com.fashionsaas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para la gestión de inventario de la tienda.
 * Consume la API de Django para obtener productos y estadísticas de inventario.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RestTemplate restTemplate;

    @Value("${app.django.api.url}")
    private String djangoApiUrl;

    /**
     * Obtiene la lista paginada de productos de la tienda autenticada.
     *
     * @param djangoToken token de acceso de Django
     * @param status      filtro opcional por estado del producto
     * @param category    filtro opcional por categoría
     * @param page        número de página
     * @return mapa con los productos y datos de paginación
     */
    public Map<String, Object> getStoreProducts(String djangoToken, String status, String category, int page) {
        StringBuilder url = new StringBuilder(djangoApiUrl + "/products/my-store/?page=" + page);
        if (status != null) url.append("&status=").append(status);
        if (category != null) url.append("&category=").append(category);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                buildAuthRequest(djangoToken),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        return response.getBody() != null ? response.getBody() : new HashMap<>();
    }

    /**
     * Obtiene un resumen del inventario de la tienda.
     *
     * @param djangoToken token de acceso de Django
     * @return mapa con el resumen del inventario
     */
    public Map<String, Object> getInventorySummary(String djangoToken) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                djangoApiUrl + "/stores/my-store/dashboard/",
                HttpMethod.GET,
                buildAuthRequest(djangoToken),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> dashboardData = response.getBody();
        Map<String, Object> summary = new HashMap<>();
        if (dashboardData != null) {
            summary.put("total_products", dashboardData.get("total_products"));
            summary.put("active_products", dashboardData.get("total_products"));
            summary.put("inactive_products", 0);
            summary.put("out_of_stock", 0);
        }
        return summary;
    }

    /**
     * Construye una petición HTTP con el token de Django
     * en el header Authorization.
     *
     * @param djangoToken token de acceso de Django
     * @return entidad HTTP con headers configurados
     */
    private HttpEntity<Void> buildAuthRequest(String djangoToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + djangoToken);
        return new HttpEntity<>(headers);
    }
}