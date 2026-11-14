package com.sfax2.annuaire.service;

import com.sfax2.annuaire.domain.Category;
import com.sfax2.annuaire.domain.Contact;
import com.sfax2.annuaire.domain.Establishment;
import com.sfax2.annuaire.domain.Kind;
import com.sfax2.annuaire.repo.EstablishmentRepository;
import com.sfax2.annuaire.repo.EstablishmentSpecs;
import com.sfax2.annuaire.service.Dtos.ContactDto;
import com.sfax2.annuaire.service.Dtos.EstablishmentDto;
import com.sfax2.annuaire.service.Dtos.EstablishmentRequest;
import com.sfax2.annuaire.service.Dtos.Facets;
import com.sfax2.annuaire.service.Dtos.PageResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EstablishmentService {

    private final EstablishmentRepository repo;

    public EstablishmentService(EstablishmentRepository repo) {
        this.repo = repo;
    }

    public PageResponse<EstablishmentDto> search(Collection<Category> categories, Kind kind, String delegation,
                                                 String q, Collection<Long> ids, int page, int size) {
        var spec = EstablishmentSpecs.of(categories, kind, delegation, q, ids);
        Page<Establishment> result = repo.findAll(spec, PageRequest.of(page, size, Sort.by("id")));
        List<EstablishmentDto> content = result.getContent().stream().map(this::toDto).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages(), result.hasNext());
    }

    /** Nombre de résultats par catégorie pour une recherche donnée (badges des onglets). */
    public Map<Category, Long> counts(String q) {
        Map<Category, Long> out = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            out.put(c, repo.count(EstablishmentSpecs.of(List.of(c), null, null, q, null)));
        }
        return out;
    }

    /** Effectifs par type et par délégation, indépendamment du filtre déjà choisi (pastilles). */
    public Facets facets(Collection<Category> categories, String q) {
        List<Establishment> all = repo.findAll(EstablishmentSpecs.of(categories, null, null, q, null));
        Map<String, Long> kinds = new LinkedHashMap<>();
        Map<String, Long> delegations = new LinkedHashMap<>();
        for (Establishment e : all) {
            if (e.getKind() != null) {
                kinds.merge(e.getKind().name(), 1L, Long::sum);
            }
            if (e.getDelegation() != null) {
                delegations.merge(e.getDelegation(), 1L, Long::sum);
            }
        }
        return new Facets(kinds, delegations);
    }

    public EstablishmentDto get(Long id) {
        return toDto(find(id));
    }

    @Transactional
    public EstablishmentDto create(EstablishmentRequest req) {
        Establishment e = new Establishment();
        apply(e, req);
        return toDto(repo.save(e));
    }

    @Transactional
    public int createAll(List<EstablishmentRequest> requests) {
        List<Establishment> list = new ArrayList<>(requests.size());
        for (EstablishmentRequest r : requests) {
            Establishment e = new Establishment();
            apply(e, r);
            list.add(e);
        }
        return repo.saveAll(list).size();
    }

    @Transactional
    public EstablishmentDto update(Long id, EstablishmentRequest req) {
        Establishment e = find(id);
        apply(e, req);
        return toDto(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(find(id));
    }

    public long count() {
        return repo.count();
    }

    /* ---------- internals ---------- */

    private Establishment find(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Établissement introuvable : " + id));
    }

    private void apply(Establishment e, EstablishmentRequest r) {
        e.setCategory(r.category());
        e.setKind(r.kind());
        e.setDelegation(blankToNull(r.delegation()));
        e.setNumber(r.number());
        e.setName(r.name().trim());
        e.setEmail(blankToNull(r.email()));
        e.setAddress(blankToNull(r.address()));
        e.setNote(blankToNull(r.note()));
        e.setPhones(new ArrayList<>(clean(r.phones())));

        e.getContacts().clear();
        if (r.contacts() != null) {
            for (ContactDto cd : r.contacts()) {
                Contact c = new Contact();
                c.setRole(cd.role());
                c.setFullName(blankToNull(cd.fullName()));
                c.setPhones(new ArrayList<>(clean(cd.phones())));
                e.addContact(c);
            }
        }
        e.setSearchText(searchText(e));
    }

    static String searchText(Establishment e) {
        StringBuilder sb = new StringBuilder();
        append(sb, e.getName());
        append(sb, e.getDelegation());
        append(sb, e.getEmail());
        append(sb, e.getAddress());
        append(sb, e.getNote());
        e.getPhones().forEach(p -> append(sb, p));
        for (Contact c : e.getContacts()) {
            append(sb, c.getFullName());
            c.getPhones().forEach(p -> append(sb, p));
        }
        return ArabicNormalizer.normalize(sb.toString());
    }

    private static void append(StringBuilder sb, String s) {
        if (s != null && !s.isBlank()) {
            sb.append(' ').append(s);
        }
    }

    private static List<String> clean(List<String> in) {
        if (in == null) {
            return List.of();
        }
        return in.stream().filter(s -> s != null && !s.isBlank()).map(s -> s.replaceAll("\\s+", "")).collect(Collectors.toList());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private EstablishmentDto toDto(Establishment e) {
        List<ContactDto> contacts = e.getContacts().stream()
                .map(c -> new ContactDto(c.getRole(), c.getFullName(), List.copyOf(c.getPhones())))
                .toList();
        return new EstablishmentDto(e.getId(), e.getCategory(), e.getKind(), e.getDelegation(), e.getNumber(),
                e.getName(), e.getEmail(), e.getAddress(), e.getNote(), List.copyOf(e.getPhones()), contacts);
    }
}
