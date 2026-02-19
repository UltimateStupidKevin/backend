package de.technikerarbeit.backend.mastergames;

import de.technikerarbeit.backend.mastergames.dto.MasterGameDetailView;
import de.technikerarbeit.backend.mastergames.dto.MasterGamePageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/master-games")
public class MasterGameController {

    private final MasterGameService service;

    public MasterGameController(MasterGameService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<MasterGamePageResponse> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "white", required = false) String white,
            @RequestParam(name = "black", required = false) String black,
            @RequestParam(name = "event", required = false) String event,
            @RequestParam(name = "result", required = false) String result,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(service.list(q, white, black, event, result, from, to, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") long id) {
        return service.get(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("master_game_not_found"));
    }
}
