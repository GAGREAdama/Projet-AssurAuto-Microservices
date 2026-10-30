package com.assurauto.sinistre.api.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prouve la configuration centralisée (valeur lue depuis le config-service, rafraîchissable via
 * POST /actuator/refresh) et la répartition de charge (le port change selon l'instance qui répond).
 */
@RestController
@RefreshScope
@RequestMapping("/api/info")
public class InfoController {

    @Value("${spring.application.name}")
    private String application;

    @Value("${assurauto.message:AssurAuto}")
    private String message;

    @Value("${server.port:8080}")
    private String port;

    @GetMapping
    public Map<String, String> info() {
        return Map.of("service", application, "message", message, "port", port);
    }
}
