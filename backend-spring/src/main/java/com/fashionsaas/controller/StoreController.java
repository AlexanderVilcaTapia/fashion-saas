package com.fashionsaas.controller;

import com.fashionsaas.dto.request.UpdateStoreRequest;
import com.fashionsaas.dto.response.StoreResponse;
import com.fashionsaas.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para la gestión del perfil de la tienda.
 * Permite al dueño ver y actualizar la información de su tienda.
 */
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@Tag(name = "Tienda", description = "Gestión del perfil de la tienda")
@SecurityRequirement(name = "bearerAuth")
public class StoreController {

    private final StoreService storeService;

    /**
     * Retorna la información del perfil de la tienda autenticada.
     *
     * @param userDetails usuario autenticado extraído del JWT
     * @return datos del perfil de la tienda
     */
    @GetMapping("/profile")
    @Operation(summary = "Perfil de la tienda", description = "Retorna los datos del perfil de la tienda autenticada")
    public ResponseEntity<StoreResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        StoreResponse store = storeService.getStoreProfile(userDetails.getUsername());
        return ResponseEntity.ok(store);
    }

    /**
     * Actualiza la información del perfil de la tienda autenticada.
     *
     * @param userDetails usuario autenticado extraído del JWT
     * @param request     datos a actualizar
     * @return perfil actualizado de la tienda
     */
    @PatchMapping("/profile")
    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del perfil de la tienda")
    public ResponseEntity<StoreResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
        StoreResponse store = storeService.updateStoreProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(store);
    }
}