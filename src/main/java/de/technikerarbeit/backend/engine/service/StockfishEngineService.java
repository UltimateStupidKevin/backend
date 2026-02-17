package de.technikerarbeit.backend.engine.service;

import de.technikerarbeit.backend.engine.dto.EngineAnalysisRequestDto;
import de.technikerarbeit.backend.engine.dto.EngineAnalysisResponseDto;
import de.technikerarbeit.backend.engine.dto.EngineLineDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Service
public class StockfishEngineService {

    private static final int DEFAULT_MOVETIME_MS = 800;
    private static final int MAX_MOVETIME_MS = 10000;

    private static final int DEFAULT_DEPTH = 14;
    private static final int MAX_DEPTH = 25;

    private static final int DEFAULT_MULTIPV = 1;
    private static final int MAX_MULTIPV = 5;

    private static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration DEFAULT_ANALYSIS_TIMEOUT = Duration.ofSeconds(15);

    private final String stockfishPath;
    private final int defaultThreads;
    private final int defaultHashMb;

    private final Semaphore concurrencyLimiter;

    public StockfishEngineService(
            @Value("${engine.stockfish.path:stockfish}") String stockfishPath,
            @Value("${engine.stockfish.threads:2}") int defaultThreads,
            @Value("${engine.stockfish.hash-mb:64}") int defaultHashMb,
            @Value("${engine.stockfish.max-concurrent:2}") int maxConcurrent
    ) {
        this.stockfishPath = stockfishPath;
        this.defaultThreads = Math.max(1, defaultThreads);
        this.defaultHashMb = Math.max(16, defaultHashMb);

        int permits = Math.max(1, maxConcurrent);
        this.concurrencyLimiter = new Semaphore(permits, true);
    }

    public String getEngineName() {
        try (UciSession session = UciSession.start(stockfishPath, DEFAULT_STARTUP_TIMEOUT)) {
            return session.getEngineName();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Stockfish konnte nicht gestartet werden. Prüfe engine.stockfish.path oder ob Stockfish im PATH ist (properties.yml). "
                    + "Details: " + e.getMessage(), e
            );
        }
    }

    public EngineAnalysisResponseDto analyse(EngineAnalysisRequestDto request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Request body fehlt.");
        }

        String fen = request.fen();
        if (fen == null || fen.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "FEN fehlt.");
        }

        validateFenBasic(fen);

        char sideToMove = extractSideToMove(fen); // 'w' oder 'b'
        boolean invertToWhite = (sideToMove == 'b');

        int multiPv = clamp(orDefault(request.multiPv(), DEFAULT_MULTIPV), 1, MAX_MULTIPV);

        Integer requestedDepth = request.depth();
        Integer requestedMovetime = request.movetimeMs();

        AnalysisMode mode = decideMode(requestedDepth, requestedMovetime);

        boolean acquired = false;
        try {
            acquired = tryAcquireLimiter(Duration.ofSeconds(1));
            if (!acquired) {
                throw new ResponseStatusException(
                        TOO_MANY_REQUESTS,
                        "Zu viele parallele Engine-Analysen. Bitte kurz warten und erneut versuchen."
                );
            }

            try (UciSession session = UciSession.start(stockfishPath, DEFAULT_STARTUP_TIMEOUT)) {
                session.setOption("Threads", String.valueOf(defaultThreads));
                session.setOption("Hash", String.valueOf(defaultHashMb));
                session.setOption("MultiPV", String.valueOf(multiPv));
                session.ucinewgame();

                session.positionFen(fen);

                Duration timeout = DEFAULT_ANALYSIS_TIMEOUT;
                if (mode.kind == AnalysisKind.MOVETIME) {
                    timeout = Duration.ofMillis(Math.min(MAX_MOVETIME_MS, mode.movetimeMs) + 10_000L);
                }

                UciAnalysisResult result = session.go(mode, timeout);

                List<EngineLineDto> lines = result.lines.values().stream()
                        .sorted(Comparator.comparingInt(l -> l.rank))
                        .map(l -> {
                            Integer cp = l.scoreCp;
                            Integer mate = l.mate;

                            if (invertToWhite) {
                                if (cp != null) cp = -cp;
                                if (mate != null) mate = -mate;
                            }

                            return new EngineLineDto(
                                    l.rank,
                                    l.depth,
                                    cp,
                                    mate,
                                    List.copyOf(l.pv)
                            );
                        })
                        .toList();

                return new EngineAnalysisResponseDto(result.bestMoveUci, "white", lines);
            }

        } catch (IOException e) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Stockfish konnte nicht gestartet/benutzt werden. Prüfe engine.stockfish.path. Details: " + e.getMessage(),
                    e
            );
        } finally {
            if (acquired) {
                concurrencyLimiter.release();
            }
        }
    }

    private boolean tryAcquireLimiter(Duration timeout) {
        try {
            return concurrencyLimiter.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void validateFenBasic(String fen) {
        String trimmed = fen.trim();
        if (trimmed.length() > 200) {
            throw new ResponseStatusException(BAD_REQUEST, "FEN ist unplausibel lang.");
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length < 2) {
            throw new ResponseStatusException(BAD_REQUEST, "FEN ist ungültig (mind. 2 Felder erwartet).");
        }

        String board = parts[0];
        String[] ranks = board.split("/");
        if (ranks.length != 8) {
            throw new ResponseStatusException(BAD_REQUEST, "FEN ist ungültig (Board muss 8 Reihen haben).");
        }
    }

    private static char extractSideToMove(String fen) {
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "FEN ist ungültig (side-to-move fehlt).");
        }
        char c = parts[1].charAt(0);
        if (c != 'w' && c != 'b') {
            throw new ResponseStatusException(BAD_REQUEST, "FEN ist ungültig (side-to-move muss 'w' oder 'b' sein).");
        }
        return c;
    }

    private static int orDefault(Integer value, int def) {
        return value == null ? def : value;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static AnalysisMode decideMode(Integer depth, Integer movetimeMs) {
        if (depth != null && movetimeMs != null) {
            int d = clamp(depth, 1, MAX_DEPTH);
            return AnalysisMode.depth(d);
        }

        if (depth != null) {
            int d = clamp(depth, 1, MAX_DEPTH);
            return AnalysisMode.depth(d);
        }

        if (movetimeMs != null) {
            int ms = clamp(movetimeMs, 50, MAX_MOVETIME_MS);
            return AnalysisMode.movetime(ms);
        }

        return AnalysisMode.depth(DEFAULT_DEPTH);
    }

    private enum AnalysisKind { DEPTH, MOVETIME }

    private static final class AnalysisMode {
        private final AnalysisKind kind;
        private final int depth;
        private final int movetimeMs;

        private AnalysisMode(AnalysisKind kind, int depth, int movetimeMs) {
            this.kind = kind;
            this.depth = depth;
            this.movetimeMs = movetimeMs;
        }

        static AnalysisMode depth(int depth) {
            return new AnalysisMode(AnalysisKind.DEPTH, depth, 0);
        }

        static AnalysisMode movetime(int movetimeMs) {
            return new AnalysisMode(AnalysisKind.MOVETIME, 0, movetimeMs);
        }
    }

    private static final class UciAnalysisLine {
        final int rank;
        int depth;
        Integer scoreCp;
        Integer mate;
        List<String> pv = List.of();

        UciAnalysisLine(int rank) {
            this.rank = rank;
        }
    }

    private static final class UciAnalysisResult {
        final String bestMoveUci;
        final Map<Integer, UciAnalysisLine> lines;

        UciAnalysisResult(String bestMoveUci, Map<Integer, UciAnalysisLine> lines) {
            this.bestMoveUci = bestMoveUci;
            this.lines = lines;
        }
    }

    private static final class UciSession implements Closeable {
        private final Process process;
        private final BufferedWriter writer;
        private final BufferedReader reader;

        private String engineName = "Stockfish";

        private UciSession(Process process) {
            this.process = process;
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        }

        static UciSession start(String executable, Duration startupTimeout) throws IOException {
            Process process = new ProcessBuilder(executable)
                    .redirectErrorStream(true)
                    .start();

            UciSession session = new UciSession(process);
            try {
                session.handshake(startupTimeout);
                return session;
            } catch (RuntimeException ex) {
                session.close();
                throw ex;
            }
        }

        String getEngineName() {
            return engineName;
        }

        void handshake(Duration timeout) {
            send("uci");
            long deadline = System.nanoTime() + timeout.toNanos();

            boolean sawUciOk = false;

            while (System.nanoTime() < deadline) {
                String line = readLineNonNull();
                if (line == null) {
                    continue;
                }

                if (line.startsWith("id name ")) {
                    engineName = line.substring("id name ".length()).trim();
                } else if (line.equals("uciok")) {
                    sawUciOk = true;
                    break;
                }
            }

            if (!sawUciOk) {
                throw new IllegalStateException("UCI handshake fehlgeschlagen (uciok nicht erhalten).");
            }

            isReady(timeout);
        }

        void isReady(Duration timeout) {
            send("isready");
            long deadline = System.nanoTime() + timeout.toNanos();

            while (System.nanoTime() < deadline) {
                String line = readLineNonNull();
                if (line == null) {
                    continue;
                }
                if (line.equals("readyok")) {
                    return;
                }
            }

            throw new IllegalStateException("Stockfish antwortet nicht (readyok nicht erhalten).");
        }

        void ucinewgame() {
            send("ucinewgame");
            send("isready");
            waitFor("readyok", Duration.ofSeconds(2));
        }

        void setOption(String name, String value) {
            send("setoption name " + name + " value " + value);
        }

        void positionFen(String fen) {
            send("position fen " + fen.trim());
        }

        UciAnalysisResult go(AnalysisMode mode, Duration timeout) {
            String goCmd = switch (mode.kind) {
                case DEPTH -> "go depth " + mode.depth;
                case MOVETIME -> "go movetime " + mode.movetimeMs;
            };

            Map<Integer, UciAnalysisLine> bestLines = new HashMap<>();
            send(goCmd);

            long deadline = System.nanoTime() + timeout.toNanos();
            String bestMove = null;

            while (System.nanoTime() < deadline) {
                String line = readLineNonNull();
                if (line == null) {
                    continue;
                }

                if (line.startsWith("info ")) {
                    parseInfoLine(line, bestLines);
                    continue;
                }

                if (line.startsWith("bestmove ")) {
                    String[] parts = line.split("\\s+");
                    bestMove = parts.length >= 2 ? parts[1] : null;
                    break;
                }
            }

            if (bestMove == null || bestMove.isBlank()) {
                throw new IllegalStateException("Kein bestmove erhalten (Timeout oder Engine-Fehler).");
            }

            return new UciAnalysisResult(bestMove, bestLines);
        }

        private void parseInfoLine(String line, Map<Integer, UciAnalysisLine> bestLines) {
            String[] t = line.split("\\s+");

            int depth = 0;
            int multipv = 1;
            Integer scoreCp = null;
            Integer mate = null;
            List<String> pv = List.of();

            for (int i = 0; i < t.length; i++) {
                String tok = t[i];

                if ("depth".equals(tok) && i + 1 < t.length) {
                    depth = tryParseInt(t[i + 1], 0);
                    continue;
                }

                if ("multipv".equals(tok) && i + 1 < t.length) {
                    multipv = Math.max(1, tryParseInt(t[i + 1], 1));
                    continue;
                }

                if ("score".equals(tok) && i + 2 < t.length) {
                    String kind = t[i + 1];
                    String val = t[i + 2];

                    if ("cp".equals(kind)) {
                        scoreCp = tryParseInt(val, null);
                        mate = null;
                    } else if ("mate".equals(kind)) {
                        mate = tryParseInt(val, null);
                        scoreCp = null;
                    }
                    continue;
                }

                if ("pv".equals(tok) && i + 1 < t.length) {
                    List<String> moves = new ArrayList<>();
                    for (int j = i + 1; j < t.length; j++) {
                        moves.add(t[j]);
                    }
                    pv = moves;
                    break;
                }
            }

            UciAnalysisLine existing = bestLines.get(multipv);
            if (existing == null || depth >= existing.depth) {
                UciAnalysisLine nl = new UciAnalysisLine(multipv);
                nl.depth = depth;
                nl.scoreCp = scoreCp;
                nl.mate = mate;
                nl.pv = pv;
                bestLines.put(multipv, nl);
            }
        }

        private static Integer tryParseInt(String s, Integer def) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        private static int tryParseInt(String s, int def) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        private void waitFor(String expected, Duration timeout) {
            long deadline = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadline) {
                String line = readLineNonNull();
                if (line == null) {
                    continue;
                }
                if (expected.equals(line)) {
                    return;
                }
            }
        }

        private void send(String cmd) {
            try {
                writer.write(cmd);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                throw new IllegalStateException("Konnte nicht an Engine schreiben: " + e.getMessage(), e);
            }
        }

        private String readLineNonNull() {
            try {
                if (!reader.ready()) {
                    Thread.sleep(5);
                    if (!reader.ready()) {
                        return null;
                    }
                }
                return reader.readLine();
            } catch (IOException e) {
                throw new IllegalStateException("Konnte nicht von Engine lesen: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Engine-Lesen unterbrochen.", e);
            }
        }

        @Override
        public void close() {
            try {
                try {
                    send("quit");
                } catch (RuntimeException ignored) {
                    // ignore
                }

                writer.close();
                reader.close();
            } catch (IOException ignored) {
                // ignore
            } finally {
                process.destroy();
                try {
                    if (!process.waitFor(200, TimeUnit.MILLISECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    process.destroyForcibly();
                }
            }
        }
    }
}
