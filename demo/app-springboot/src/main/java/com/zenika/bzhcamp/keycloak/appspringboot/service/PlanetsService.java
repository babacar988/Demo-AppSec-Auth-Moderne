package com.zenika.bzhcamp.keycloak.appspringboot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PlanetsService {

    private final WebClient webClient;
    private final String endpoint;

    /**
     * Injection par constructeur : @Value injecte l'URL depuis application.yml (planets.service.url).
     * Le WebClient transmet le Bearer token fourni par le contrôleur vers service-planets.
     * Ce token est l'access token OIDC de l'utilisateur connecté, récupéré depuis OidcUser.
     */
    public PlanetsService(WebClient webClient, @Value("${planets.service.url}") String endpoint) {
        this.webClient = webClient;
        this.endpoint = endpoint;
    }

    public List<String> getPlanets(String bearerToken) {
        return webClient
                .get()
                .uri(endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block();
    }
}