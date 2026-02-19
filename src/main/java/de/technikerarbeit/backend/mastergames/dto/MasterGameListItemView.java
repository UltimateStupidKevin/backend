package de.technikerarbeit.backend.mastergames.dto;

import de.technikerarbeit.backend.mastergames.MasterGame;

import java.time.LocalDate;

public class MasterGameListItemView {

    public Long id;
    public String event;
    public String site;
    public LocalDate gameDate;
    public String white;
    public String black;
    public String result;

    public static MasterGameListItemView of(MasterGame g) {
        MasterGameListItemView v = new MasterGameListItemView();
        v.id = g.getId();
        v.event = g.getEvent();
        v.site = g.getSite();
        v.gameDate = g.getGameDate();
        v.white = g.getWhite();
        v.black = g.getBlack();
        v.result = g.getResult();
        return v;
    }
}
