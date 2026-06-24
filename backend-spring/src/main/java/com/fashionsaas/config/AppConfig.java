package com.fashionsaas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración general de beans de la aplicación.
 * Define el RestTemplate para consumir la API de Django.
 */
@Configuration
public class AppConfig {

    /**
     * Bean de RestTemplate para realizar peticiones HTTP
     * hacia la API principal de Django.
     *
     * @return instancia de RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}