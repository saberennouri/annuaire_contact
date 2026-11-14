package com.sfax2.annuaire.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Page d'accueil minimale de l'API (évite un 403 déroutant sur « / »). */
@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "annuaire-sfax2-api",
                "docs", "/swagger-ui.html",
                "exemple", "/api/establishments/counts");
    }
}
