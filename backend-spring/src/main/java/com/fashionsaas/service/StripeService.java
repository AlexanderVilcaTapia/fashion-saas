package com.fashionsaas.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para la integración de pagos con Stripe.
 * Gestiona la creación de payment intents y el procesamiento de webhooks.
 */
@Service
@RequiredArgsConstructor
public class StripeService {

    private final RestTemplate restTemplate;

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${app.django.api.url}")
    private String djangoApiUrl;

    /**
     * Inicializa la clave secreta de Stripe al arrancar el servicio.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Crea un payment intent en Stripe para procesar el pago de una orden.
     * El monto debe enviarse en centavos (ej: S/. 100.00 = 10000).
     *
     * @param amount   monto en centavos
     * @param currency código de moneda (usd, pen, etc.)
     * @param orderId  identificador de la orden asociada
     * @return mapa con el client secret del payment intent
     */
    public Map<String, Object> createPaymentIntent(Long amount, String currency, Long orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .putMetadata("order_id", orderId.toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            response.put("client_secret", paymentIntent.getClientSecret());
            response.put("payment_intent_id", paymentIntent.getId());
            response.put("amount", amount);
            response.put("currency", currency);
            response.put("status", paymentIntent.getStatus());

        } catch (StripeException e) {
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * Procesa los webhooks enviados por Stripe.
     * Verifica la firma del webhook y actualiza el estado de pago
     * de la orden en Django cuando el pago es exitoso.
     *
     * @param payload   cuerpo del webhook
     * @param signature firma del webhook para verificación
     * @return mapa con el resultado del procesamiento
     */
    public Map<String, Object> handleWebhook(String payload, String signature) {
        Map<String, Object> response = new HashMap<>();
        try {
            Event event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);

            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);

                if (paymentIntent != null) {
                    String orderId = paymentIntent.getMetadata().get("order_id");
                    updateOrderPaymentStatus(orderId, "paid", paymentIntent.getId());
                }
            }

            response.put("received", true);
            response.put("type", event.getType());

        } catch (SignatureVerificationException e) {
            response.put("error", "Firma inválida: " + e.getMessage());
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * Actualiza el estado de pago de una orden en Django
     * una vez que Stripe confirma el pago exitoso.
     *
     * @param orderId         identificador de la orden
     * @param paymentStatus   nuevo estado de pago
     * @param paymentIntentId identificador del payment intent de Stripe
     */
    private void updateOrderPaymentStatus(
            String orderId,
            String paymentStatus,
            String paymentIntentId
    ) {
        try {
            String url = djangoApiUrl + "/orders/my-store/" + orderId + "/";
            Map<String, String> body = Map.of(
                    "payment_status", paymentStatus,
                    "payment_intent_id", paymentIntentId
            );
            restTemplate.patchForObject(url, body, Map.class);
        } catch (Exception e) {
            System.err.println("Error actualizando orden en Django: " + e.getMessage());
        }
    }
}