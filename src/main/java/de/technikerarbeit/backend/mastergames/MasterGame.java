package de.technikerarbeit.backend.mastergames;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "master_games")
public class MasterGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event")
    private String event;

    @Column(name = "site")
    private String site;

    @Column(name = "game_date")
    private LocalDate gameDate;

    @Column(name = "white")
    private String white;

    @Column(name = "black")
    private String black;

    @Column(name = "result", length = 7)
    private String result;

    @Lob
    @Column(name = "pgn", nullable = false, columnDefinition = "LONGTEXT")
    private String pgn;

    public Long getId() {
        return id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public LocalDate getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDate gameDate) {
        this.gameDate = gameDate;
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPgn() {
        return pgn;
    }

    public void setPgn(String pgn) {
        this.pgn = pgn;
    }
}
