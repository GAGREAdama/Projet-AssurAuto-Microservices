package com.assurauto.sinistre.infrastructure.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Notification de l'historique commercial du client dans client-service.
 */
@Component
public class ClientGateway {

    private static final Logger log =
            LoggerFactory.getLogger(ClientGateway.class);

    private final RestClient restClient;

    public ClientGateway(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,
            @Value("${assurauto.internal.username:agent}") String username,
            @Value("${assurauto.internal.password:agent123}") String password) {

        this.restClient = builder
                .baseUrl("http://client-service")
                .defaultHeaders(headers ->
                        headers.setBasicAuth(username, password))
                .build();
    }

    public void enregistrerEvenement(
            Long clientId,
            String type,
            String description) {

        try {
            restClient.post()
                    .uri("/api/clients/{id}/historique", clientId)
                    .body(Map.of(
                            "type", type,
                            "description", description))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RuntimeException e) {
            log.warn(
                    "Historique commercial non mis à jour pour le client {} : {}",
                    clientId,
                    e.getMessage());
        }
    }
}