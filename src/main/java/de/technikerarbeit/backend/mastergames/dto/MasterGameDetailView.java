package de.technikerarbeit.backend.mastergames.dto;

import de.technikerarbeit.backend.mastergames.MasterGame;

import java.time.LocalDate;
import java.util.Map;

public class MasterGameDetailView {

    public Long id;
    public String event;
    public String site;
    public LocalDate gameDate;
    public String white;
    public String black;
    public String result;

    public String initialFen;
    public Map<String, String> tags;

    public String pgn;

    public static MasterGameDetailView of(MasterGame g, String initialFen, Map<String, String> tags) {
        MasterGameDetailView v = new MasterGameDetailView();
        v.id = g.getId();
        v.event = g.getEvent();
        v.site = g.getSite();
        v.gameDate = g.getGameDate();
        v.white = g.getWhite();
        v.black = g.getBlack();
        v.result = g.getResult();
        v.initialFen = initialFen;
        v.tags = tags;
        v.pgn = g.getPgn();
        return v;
    }
}
