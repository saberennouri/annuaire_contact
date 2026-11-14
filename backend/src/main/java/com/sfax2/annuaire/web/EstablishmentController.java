package com.sfax2.annuaire.web;

import com.sfax2.annuaire.domain.Category;
import com.sfax2.annuaire.domain.Kind;
import com.sfax2.annuaire.service.Dtos.EstablishmentDto;
import com.sfax2.annuaire.service.Dtos.EstablishmentRequest;
import com.sfax2.annuaire.service.Dtos.Facets;
import com.sfax2.annuaire.service.Dtos.PageResponse;
import com.sfax2.annuaire.service.EstablishmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/establishments")
public class EstablishmentController {

    private final EstablishmentService service;

    public EstablishmentController(EstablishmentService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EstablishmentDto> search(
            @RequestParam(required = false) List<Category> category,
            @RequestParam(required = false) Kind kind,
            @RequestParam(required = false) String delegation,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return service.search(category, kind, delegation, q, ids, page, size);
    }

    @GetMapping("/counts")
    public Map<Category, Long> counts(@RequestParam(required = false) String q) {
        return service.counts(q);
    }

    @GetMapping("/facets")
    public Facets facets(@RequestParam(required = false) List<Category> category,
                         @RequestParam(required = false) String q) {
        return service.facets(category, q);
    }

    @GetMapping("/{id}")
    public EstablishmentDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<EstablishmentDto> create(@Valid @RequestBody EstablishmentRequest request) {
        EstablishmentDto created = service.create(request);
        return ResponseEntity.created(URI.create("/api/establishments/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EstablishmentDto update(@PathVariable Long id, @Valid @RequestBody EstablishmentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
