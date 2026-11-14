package com.sfax2.annuaire.repo;

import com.sfax2.annuaire.domain.Category;
import com.sfax2.annuaire.domain.Establishment;
import com.sfax2.annuaire.domain.Kind;
import com.sfax2.annuaire.service.ArabicNormalizer;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Construit les critères de recherche dynamiques. */
public final class EstablishmentSpecs {

    private EstablishmentSpecs() {
    }

    public static Specification<Establishment> of(Collection<Category> categories, Kind kind, String delegation,
                                                  String q, Collection<Long> ids) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (categories != null && !categories.isEmpty()) {
                ps.add(root.get("category").in(categories));
            }
            if (kind != null) {
                ps.add(cb.equal(root.get("kind"), kind));
            }
            if (delegation != null && !delegation.isBlank()) {
                ps.add(cb.equal(root.get("delegation"), delegation));
            }
            if (ids != null && !ids.isEmpty()) {
                ps.add(root.get("id").in(ids));
            }
            for (String token : ArabicNormalizer.tokens(q)) {
                ps.add(cb.like(root.<String>get("searchText"), "%" + escapeLike(token) + "%", '\\'));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
