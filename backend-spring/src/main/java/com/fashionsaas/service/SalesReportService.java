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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la generación de reportes de ventas.
 * Consulta la API de Django y procesa los datos para generar
 * reportes por rango de fechas.
 */
@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final RestTemplate restTemplate;

    @Value("${app.django.api.url}")
    private String djangoApiUrl;

    /**
     * Genera un reporte de ventas para la tienda autenticada
     * en el rango de fechas indicado.
     *
     * @param email     correo del dueño de tienda autenticado
     * @param startDate fecha de inicio en formato YYYY-MM-DD
     * @param endDate   fecha de fin en formato YYYY-MM-DD
     * @return mapa con el reporte de ventas del período
     */
    public Map<String, Object> getSalesReport(String email, String startDate, String endDate) {
        String url = djangoApiUrl + "/orders/my-store/?payment_status=paid"
                + "&created_after=" + startDate
                + "&created_before=" + endDate;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                buildAuthRequest(email),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> body = response.getBody();
        if (body == null) return buildEmptyReport(startDate, endDate);

        List<Map<String, Object>> orders = (List<Map<String, Object>>) body.get("results");
        if (orders == null) return buildEmptyReport(startDate, endDate);

        double totalRevenue = orders.stream()
                .mapToDouble(o -> parseDouble(o.get("total")))
                .sum();

        int totalOrders = orders.size();

        Map<String, Double> revenueByDay = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> ((String) o.get("created_at")).substring(0, 10),
                        Collectors.summingDouble(o -> parseDouble(o.get("total")))
                ));

        Map<String, Object> report = new HashMap<>();
        report.put("start_date", startDate);
        report.put("end_date", endDate);
        report.put("total_revenue", totalRevenue);
        report.put("total_orders", totalOrders);
        report.put("average_order_value", totalOrders > 0 ? totalRevenue / totalOrders : 0);
        report.put("revenue_by_day", revenueByDay);

        return report;
    }

    /**
     * Construye un reporte vacío cuando no hay datos en el período.
     *
     * @param startDate fecha de inicio
     * @param endDate   fecha de fin
     * @return mapa con valores en cero
     */
    private Map<String, Object> buildEmptyReport(String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("start_date", startDate);
        report.put("end_date", endDate);
        report.put("total_revenue", 0.0);
        report.put("total_orders", 0);
        report.put("average_order_value", 0.0);
        report.put("revenue_by_day", new HashMap<>());
        return report;
    }

    /**
     * Construye una petición HTTP con el email del usuario
     * en los headers para identificarlo en Django.
     *
     * @param email correo del usuario autenticado
     * @return entidad HTTP con headers configurados
     */
    private HttpEntity<Void> buildAuthRequest(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Email", email);
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
}