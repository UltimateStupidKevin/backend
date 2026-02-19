package de.technikerarbeit.backend.mastergames;

import de.technikerarbeit.backend.mastergames.dto.MasterGameDetailView;
import de.technikerarbeit.backend.mastergames.dto.MasterGameListItemView;
import de.technikerarbeit.backend.mastergames.dto.MasterGamePageResponse;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MasterGameService {

    private static final Pattern TAG_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"([^\"]*)\"\\]");

    private final MasterGameRepository repo;

    public MasterGameService(MasterGameRepository repo) {
        this.repo = repo;
    }

    public MasterGamePageResponse list(
            String q,
            String white,
            String black,
            String event,
            String result,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(200, Math.max(1, size));

        Sort sort = Sort.by(Sort.Order.desc("gameDate"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        var spec = MasterGameSpecifications.build(q, white, black, event, result, from, to);
        Page<MasterGame> res = repo.findAll(spec, pageable);

        var items = res.getContent().stream().map(MasterGameListItemView::of).toList();
        return new MasterGamePageResponse(items, res.getNumber(), res.getSize(), res.getTotalElements(), res.getTotalPages());
    }

    public Optional<MasterGameDetailView> get(long id) {
        return repo.findById(id).map(g -> {
            Map<String, String> tags = parseTags(g.getPgn());
            String initialFen = tags.getOrDefault("FEN", "startpos");
            return MasterGameDetailView.of(g, initialFen, tags);
        });
    }

    private static Map<String, String> parseTags(String pgn) {
        Map<String, String> tags = new LinkedHashMap<>();
        if (pgn == null || pgn.isBlank()) return tags;

        Matcher m = TAG_PATTERN.matcher(pgn);
        while (m.find()) {
            String key = m.group(1);
            String val = m.group(2);
            tags.put(key, val);
        }
        return tags;
    }
}
