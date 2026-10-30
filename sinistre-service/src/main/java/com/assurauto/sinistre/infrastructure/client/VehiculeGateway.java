package com.assurauto.sinistre.infrastructure.client;

import com.assurauto.sinistre.application.exception.ServiceUnavailableException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Anti-corruption layer vers vehicule-service.
 */
@Component
public class VehiculeGateway {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VehiculeView(Long id, Long contratId) {
    }

    private static final Logger log =
            LoggerFactory.getLogger(VehiculeGateway.class);

    private final RestClient restClient;

    public VehiculeGateway(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,
            @Value("${assurauto.internal.username:agent}") String username,
            @Value("${assurauto.internal.password:agent123}") String password) {

        this.restClient = builder
                .baseUrl("http://vehicule-service")
                .defaultHeaders(headers ->
                        headers.setBasicAuth(username, password))
                .build();
    }

    public Optional<VehiculeView> trouver(Long vehiculeId) {
        try {
            VehiculeView vehicule = restClient.get()
                    .uri("/api/vehicules/{id}", vehiculeId)
                    .retrieve()
                    .body(VehiculeView.class);

            return Optional.ofNullable(vehicule);

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();

        } catch (HttpClientErrorException e) {
            throw new ServiceUnavailableException(
                    "vehicule-service a refusé la requête ("
                            + e.getStatusCode() + ")");

        } catch (RestClientException e) {
            log.error("Appel à vehicule-service impossible : {}",
                    e.getMessage());

            throw new ServiceUnavailableException(
                    "vehicule-service est injoignable, réessayez plus tard");
        }
    }
}