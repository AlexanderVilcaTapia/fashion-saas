package com.fashionsaas.service;

import com.fashionsaas.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de órdenes desde el panel admin.
 * Consume la API de Django para listar y actualizar órdenes.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final RestTemplate restTemplate;

    @Value("${app.django.api.url}")
    private String djangoApiUrl;

    /**
     * Obtiene la lista de órdenes de la tienda autenticada.
     * Permite filtrar por estado de orden y estado de pago.
     *
     * @param djangoToken   token de acceso de Django
     * @param status        filtro opcional por estado de orden
     * @param paymentStatus filtro opcional por estado de pago
     * @return lista de órdenes de la tienda
     */
    public List<OrderResponse> getStoreOrders(String djangoToken, String status, String paymentStatus) {
        StringBuilder url = new StringBuilder(djangoApiUrl + "/orders/my-store/?format=json");
        if (status != null) url.append("&status=").append(status);
        if (paymentStatus != null) url.append("&payment_status=").append(paymentStatus);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                buildAuthRequest(djangoToken, null),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> body = response.getBody();
        if (body == null) return List.of();
        List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
        if (results == null) return List.of();
        return results.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle de una orden específica de la tienda autenticada.
     *
     * @param djangoToken token de acceso de Django
     * @param orderId     identificador de la orden
     * @return detalle de la orden
     */
    public OrderResponse getOrderDetail(String djangoToken, Long orderId) {
        String url = djangoApiUrl + "/orders/my-store/" + orderId + "/";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                buildAuthRequest(djangoToken, null),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        return mapToOrderResponse(response.getBody());
    }

    /**
     * Actualiza el estado de una orden específica de la tienda autenticada.
     *
     * @param djangoToken token de acceso de Django
     * @param orderId     identificador de la orden
     * @param newStatus   nuevo estado de la orden
     * @return orden con el estado actualizado
     */
    public OrderResponse updateOrderStatus(String djangoToken, Long orderId, String newStatus) {
        String url = djangoApiUrl + "/orders/my-store/" + orderId + "/";
        Map<String, String> body = Map.of("status", newStatus);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                buildAuthRequest(djangoToken, body),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        return mapToOrderResponse(response.getBody());
    }

    /**
     * Construye una petición HTTP con el token de Django
     * en el header Authorization.
     *
     * @param djangoToken token de acceso de Django
     * @param body        cuerpo de la petición, puede ser nulo
     * @return entidad HTTP con headers y body configurados
     */
    private HttpEntity<Object> buildAuthRequest(String djangoToken, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + djangoToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    /**
     * Mapea el mapa de respuesta de Django a un OrderResponse.
     *
     * @param body mapa con los datos de la orden
     * @return objeto OrderResponse con los datos mapeados
     */
    private OrderResponse mapToOrderResponse(Map<String, Object> body) {
        if (body == null) return new OrderResponse();
        return OrderResponse.builder()
                .id(parseLong(body.get("id")))
                .buyerName((String) body.get("buyer_name"))
                .storeName((String) body.get("store_name"))
                .status((String) body.get("status"))
                .paymentStatus((String) body.get("payment_status"))
                .subtotal(parseDouble(body.get("subtotal")))
                .shippingCost(parseDouble(body.get("shipping_cost")))
                .total(parseDouble(body.get("total")))
                .shippingAddress((String) body.get("shipping_address"))
                .shippingCity((String) body.get("shipping_city"))
                .shippingPhone((String) body.get("shipping_phone"))
                .createdAt((String) body.get("created_at"))
                .build();
    }

    /**
     * Convierte un objeto a Long de forma segura.
     *
     * @param value objeto a convertir
     * @return valor Long o null si es nulo
     */
    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
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