package de.technikerarbeit.backend.engine.dto;

import java.util.List;

public record EngineAnalysisResponseDto(
        String bestMoveUci,
        String scorePerspective,
        List<EngineLineDto> lines
) {
}
