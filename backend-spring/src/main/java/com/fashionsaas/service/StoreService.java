package com.fashionsaas.service;

import com.fashionsaas.dto.request.UpdateStoreRequest;
import com.fashionsaas.dto.response.StoreResponse;
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

import java.util.Map;

/**
 * Servicio para la gestión del perfil de la tienda.
 * Consume la API de Django para obtener y actualizar los datos.
 */
@Service
@RequiredArgsConstructor
public class StoreService {

    private final RestTemplate restTemplate;

    @Value("${app.django.api.url}")
    private String djangoApiUrl;

    /**
     * Obtiene el perfil de la tienda del dueño autenticado.
     *
     * @param djangoToken token de acceso de Django
     * @return datos del perfil de la tienda
     */
    public StoreResponse getStoreProfile(String djangoToken) {
        String url = djangoApiUrl + "/stores/my-store/";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                buildAuthRequest(djangoToken, null),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        return mapToStoreResponse(response.getBody());
    }

    /**
     * Actualiza el perfil de la tienda del dueño autenticado.
     *
     * @param djangoToken token de acceso de Django
     * @param request     datos a actualizar
     * @return perfil actualizado de la tienda
     */
    public StoreResponse updateStoreProfile(String djangoToken, UpdateStoreRequest request) {
        String url = djangoApiUrl + "/stores/my-store/";
        Map<String, Object> body = Map.of(
                "name", request.getName(),
                "description", request.getDescription() != null ? request.getDescription() : "",
                "address", request.getAddress() != null ? request.getAddress() : "",
                "city", request.getCity() != null ? request.getCity() : "",
                "phone", request.getPhone() != null ? request.getPhone() : "",
                "email", request.getEmail() != null ? request.getEmail() : ""
        );
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                buildAuthRequest(djangoToken, body),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        return mapToStoreResponse(response.getBody());
    }

    /**
     * Construye una petición HTTP con el token de Django
     * en el header Authorization para autenticar en Django.
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
     * Mapea el mapa de respuesta de Django a un StoreResponse.
     *
     * @param body mapa con los datos de la tienda
     * @return objeto StoreResponse con los datos mapeados
     */
    private StoreResponse mapToStoreResponse(Map<String, Object> body) {
        if (body == null) return new StoreResponse();
        return StoreResponse.builder()
                .id(parseLong(body.get("id")))
                .name((String) body.get("name"))
                .slug((String) body.get("slug"))
                .description((String) body.get("description"))
                .address((String) body.get("address"))
                .city((String) body.get("city"))
                .phone((String) body.get("phone"))
                .email((String) body.get("email"))
                .status((String) body.get("status"))
                .totalProducts(parseInteger(body.get("total_products")))
                .ownerName((String) body.get("owner_name"))
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