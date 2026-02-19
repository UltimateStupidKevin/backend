package de.technikerarbeit.backend.mastergames;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class MasterGameSpecifications {

    private MasterGameSpecifications() {}

    public static Specification<MasterGame> build(
            String q,
            String white,
            String black,
            String event,
            String result,
            LocalDate from,
            LocalDate to
    ) {
        Specification<MasterGame> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("event")), like),
                    cb.like(cb.lower(root.get("site")), like),
                    cb.like(cb.lower(root.get("white")), like),
                    cb.like(cb.lower(root.get("black")), like)
            ));
        }

        if (white != null && !white.isBlank()) {
            String like = "%" + white.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("white")), like));
        }

        if (black != null && !black.isBlank()) {
            String like = "%" + black.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("black")), like));
        }

        if (event != null && !event.isBlank()) {
            String like = "%" + event.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("event")), like));
        }

        if (result != null && !result.isBlank()) {
            String exact = result.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("result"), exact));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("gameDate"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("gameDate"), to));
        }

        return spec;
    }
}
