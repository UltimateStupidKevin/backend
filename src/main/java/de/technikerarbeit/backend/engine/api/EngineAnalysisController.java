package de.technikerarbeit.backend.engine.api;

import de.technikerarbeit.backend.engine.dto.EngineAnalysisRequestDto;
import de.technikerarbeit.backend.engine.dto.EngineAnalysisResponseDto;
import de.technikerarbeit.backend.engine.dto.EngineInfoDto;
import de.technikerarbeit.backend.engine.service.StockfishEngineService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/engine", produces = MediaType.APPLICATION_JSON_VALUE)
public class EngineAnalysisController {

    private final StockfishEngineService stockfishEngineService;

    public EngineAnalysisController(StockfishEngineService stockfishEngineService) {
        this.stockfishEngineService = stockfishEngineService;
    }

    @GetMapping("/ping")
    public EngineInfoDto ping() {
        String name = stockfishEngineService.getEngineName();
        return new EngineInfoDto(true, name);
    }

    @PostMapping(path = "/analyse", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EngineAnalysisResponseDto analyse(@RequestBody EngineAnalysisRequestDto request) {
        return stockfishEngineService.analyse(request);
    }
}
