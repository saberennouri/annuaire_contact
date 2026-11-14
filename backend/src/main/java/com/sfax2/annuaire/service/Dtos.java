package com.sfax2.annuaire.service;

import com.sfax2.annuaire.domain.Category;
import com.sfax2.annuaire.domain.Kind;
import com.sfax2.annuaire.domain.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/** Objets échangés avec l'API (lecture et écriture). */
public final class Dtos {

    private Dtos() {
    }

    public record ContactDto(@NotNull Role role, @Size(max = 200) String fullName,
                             List<@Size(max = 30) String> phones) {
    }

    public record EstablishmentDto(Long id, Category category, Kind kind, String delegation, Integer number,
                                   String name, String email, String address, String note,
                                   List<String> phones, List<ContactDto> contacts) {
    }

    public record EstablishmentRequest(@NotNull Category category, Kind kind, @Size(max = 60) String delegation,
                                       Integer number, @NotBlank @Size(max = 300) String name,
                                       @Size(max = 200) String email, @Size(max = 500) String address,
                                       @Size(max = 300) String note, List<@Size(max = 30) String> phones,
                                       @Valid List<ContactDto> contacts) {
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages,
                                  boolean hasNext) {
    }

    public record Facets(Map<String, Long> kinds, Map<String, Long> delegations) {
    }
}
