package com.sfax2.annuaire.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfax2.annuaire.service.Dtos.EstablishmentRequest;
import com.sfax2.annuaire.service.EstablishmentService;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** Charge les données issues des deux PDF au premier démarrage (base vide). */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class SeedLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedLoader.class);

    private final EstablishmentService service;
    private final ObjectMapper mapper;

    public SeedLoader(EstablishmentService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (service.count() > 0) {
            log.info("Base déjà remplie, seed ignoré.");
            return;
        }
        try (InputStream in = new ClassPathResource("seed/establishments.json").getInputStream()) {
            List<EstablishmentRequest> rows = mapper.readValue(in, new TypeReference<List<EstablishmentRequest>>() { });
            int n = service.createAll(rows);
            log.info("Seed chargé : {} établissements.", n);
        }
    }
}
