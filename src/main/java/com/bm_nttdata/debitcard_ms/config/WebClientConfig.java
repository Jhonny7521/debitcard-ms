package com.bm_nttdata.debitcard_ms.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración para la creación de instancias de WebClient en la aplicación.
 * Esta clase proporciona la configuración necesaria para crear clientes web reactivos
 * que pueden ser utilizados para realizar llamadas HTTP a servicios externos.
 */
@Configuration
public class WebClientConfig {

    /**
     * Crea y configura un builder para WebClient con balanceo de carga.
     *
     * @LoadBalanced habilita la integración con el balanceador de carga
     * de Spring Cloud, permitiendo la resolución de nombres de servicio
     * a través de un servidor de descubrimiento (por ejemplo, Eureka).
     * @return WebClient.Builder configurado con balanceo de carga
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder builder() {
        return WebClient.builder();
    }
}
