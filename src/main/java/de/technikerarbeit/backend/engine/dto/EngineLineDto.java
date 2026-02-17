package de.technikerarbeit.backend.engine.dto;

import java.util.List;

public record EngineLineDto(
        int rank,
        int depth,
        Integer scoreCp,
        Integer mate,
        List<String> pv
) {
}
