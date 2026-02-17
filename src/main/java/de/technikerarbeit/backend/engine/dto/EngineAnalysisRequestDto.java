package de.technikerarbeit.backend.engine.dto;

public record EngineAnalysisRequestDto(
        String fen,
        Integer depth,
        Integer movetimeMs,
        Integer multiPv
) {
}
