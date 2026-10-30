package com.assurauto.vehicule.infrastructure.client;

import com.assurauto.vehicule.application.exception.ServiceUnavailableException;
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
 * Anti-corruption layer vers contrat-service
 * (appel REST via Eureka et Spring Cloud LoadBalancer).
 */
@Component
public class ContratGateway {

    /**
     * Vue minimale d'un contrat côté vehicule-service.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContratView(Long id, Long clientId, String statut) {
    }

    private static final Logger log =
            LoggerFactory.getLogger(ContratGateway.class);

    private final RestClient restClient;

    public ContratGateway(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,
            @Value("${assurauto.internal.username:agent}") String username,
            @Value("${assurauto.internal.password:agent123}") String password) {

        this.restClient = builder
                .baseUrl("http://contrat-service")
                .defaultHeaders(headers ->
                        headers.setBasicAuth(username, password))
                .build();
    }

    public Optional<ContratView> trouver(Long contratId) {
        try {
            ContratView contrat = restClient.get()
                    .uri("/api/contrats/{id}", contratId)
                    .retrieve()
                    .body(ContratView.class);

            return Optional.ofNullable(contrat);

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();

        } catch (HttpClientErrorException e) {
            throw new ServiceUnavailableException(
                    "contrat-service a refusé la requête ("
                            + e.getStatusCode() + ")");

        } catch (RestClientException e) {
            log.error("Appel à contrat-service impossible : {}",
                    e.getMessage());

            throw new ServiceUnavailableException(
                    "contrat-service est injoignable, réessayez plus tard");
        }
    }
}