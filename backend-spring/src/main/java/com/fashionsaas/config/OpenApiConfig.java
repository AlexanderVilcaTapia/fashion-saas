package com.fashionsaas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger para el panel admin.
 * Define el esquema de seguridad JWT para autenticar
 * las peticiones desde la interfaz de Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Fashion SaaS Admin API",
                version = "1.0",
                description = "Panel administrativo para tiendas de ropa"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}