package com.fashionsaas.service;

import com.fashionsaas.dto.response.DashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio que obtiene las estadísticas del dashboard
 * consultando directamente la API de Django con el token de Django.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RestTemplate restTemplate;

    @Value("${app.django.api.url}")
    private String djangoApiUrl;

    /**
     * Obtiene las estadísticas generales de la tienda
     * desde el endpoint de Django correspondiente.
     *
     * @param djangoToken token de acceso de Django
     * @return estadísticas del dashboard de la tienda
     */
    public DashboardStatsResponse getStoreStats(String djangoToken) {
        String url = djangoApiUrl + "/orders/my-store/stats/";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                buildAuthRequest(djangoToken),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> body = response.getBody();

        if (body == null) {
            return DashboardStatsResponse.builder()
                    .totalRevenue(0.0)
                    .totalOrders(0)
                    .pendingOrders(0)
                    .totalProducts(0)
                    .build();
        }

        return DashboardStatsResponse.builder()
                .totalRevenue(parseDouble(body.get("total_revenue")))
                .totalOrders(parseInteger(body.get("total_orders")))
                .pendingOrders(parseInteger(body.get("pending_orders")))
                .totalProducts(parseInteger(body.get("total_products")))
                .build();
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

    /**
     * Convierte un objeto a Double de forma segura.
     *
     * @param value objeto a convertir
     * @return valor Double o 0.0 si es nulo
     */
    private Double parseDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }

    /**
     * Convierte un objeto a Integer de forma segura.
     *
     * @param value objeto a convertir
     * @return valor Integer o 0 si es nulo
     */
    private Integer parseInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
}