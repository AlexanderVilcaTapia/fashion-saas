package com.fashionsaas.controller;

import com.fashionsaas.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador para la integración de pagos con Stripe.
 * Gestiona la creación de payment intents y la recepción de webhooks.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Integración de pagos con Stripe")
public class StripeController {

    private final StripeService stripeService;

    /**
     * Crea un payment intent en Stripe para procesar el pago de una orden.
     *
     * @param userDetails usuario autenticado extraído del JWT
     * @param request     mapa con el monto y la moneda del pago
     * @return client secret del payment intent creado
     */
    @PostMapping("/create-intent")
    @Operation(summary = "Crear payment intent", description = "Crea un payment intent en Stripe para procesar el pago")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request
    ) {
        Long amount = Long.parseLong(request.get("amount").toString());
        String currency = (String) request.getOrDefault("currency", "usd");
        Long orderId = Long.parseLong(request.get("order_id").toString());

        Map<String, Object> response = stripeService.createPaymentIntent(amount, currency, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Recibe y procesa los webhooks de Stripe.
     * Actualiza el estado de pago de la orden cuando Stripe confirma el pago.
     *
     * @param payload   cuerpo del webhook enviado por Stripe
     * @param signature header de firma para verificar la autenticidad del webhook
     * @return confirmación de recepción del webhook
     */
    @PostMapping("/webhook")
    @Operation(summary = "Webhook de Stripe", description = "Recibe eventos de Stripe para actualizar estados de pago")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        Map<String, Object> response = stripeService.handleWebhook(payload, signature);
        return ResponseEntity.ok(response);
    }
}