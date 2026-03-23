import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Player {
    private final String name;

    // Existing: integer scores (wins, totals, high scores)
    private final Map<String, Integer> scores = new HashMap<>();

    // NEW: how many times each game has been played
    private final Map<String, Integer> gamesPlayed = new HashMap<>();

    // NEW: most recent score for each game (separate from the all-time high)
    private final Map<String, Integer> recentScores = new HashMap<>();

    // NEW: ISO date-time string of the last time each game was played
    private final Map<String, String> lastPlayed = new HashMap<>();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Player(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    // =========================================================
    // Existing score helpers (unchanged so nothing else breaks)
    // =========================================================
    public void setScore(String key, int score) { scores.put(key, score); }

    public void incrementScore(String key) { scores.put(key, getScore(key) + 1); }

    public int getScore(String key) { return scores.getOrDefault(key, 0); }

    public void addPoints(String key, int points) {
        scores.put(key, getScore(key) + points);
    }

    public Map<String, Integer> getAllScores() { return new HashMap<>(scores); }

    // =========================================================
    // NEW: Call this at the end of every game session
    // gameKey  – short identifier, e.g. "PatternChallenge"
    // score    – the score the player just earned this session
    // =========================================================
    public void recordGamePlayed(String gameKey, int score) {
        // 1. Increment total games played for this game
        gamesPlayed.put(gameKey, getGamesPlayed(gameKey) + 1);

        // 2. Store the most-recent score (always overwrite)
        recentScores.put(gameKey, score);

        // 3. Update the all-time high score if this is better
        String highKey = gameKey + "_HighScore";
        if (score > getScore(highKey)) {
            scores.put(highKey, score);
        }

        // 4. Stamp the current date-time
        lastPlayed.put(gameKey, LocalDateTime.now().format(FMT));
    }

    // =========================================================
    // NEW: Getters
    // =========================================================
    public int getGamesPlayed(String gameKey) {
        return gamesPlayed.getOrDefault(gameKey, 0);
    }

    public int getRecentScore(String gameKey) {
        return recentScores.getOrDefault(gameKey, 0);
    }

    public String getLastPlayed(String gameKey) {
        return lastPlayed.getOrDefault(gameKey, "Never");
    }

    // Total games played across ALL games combined
    public int getTotalGamesPlayed() {
        return gamesPlayed.values().stream().mapToInt(Integer::intValue).sum();
    }

    // =========================================================
    // Serialisation — extended to include new maps
    // =========================================================
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("scores", scores);
        obj.put("gamesPlayed", gamesPlayed);
        obj.put("recentScores", recentScores);
        obj.put("lastPlayed", lastPlayed);
        return obj;
    }

    public static Player fromJSON(JSONObject obj) {
        String name = obj.getString("name");
        Player p = new Player(name);

        // Existing scores
        JSONObject scoresJson = obj.getJSONObject("scores");
        for (String key : scoresJson.keySet()) {
            p.setScore(key, scoresJson.getInt(key));
        }

        // NEW: games played counts
        if (obj.has("gamesPlayed")) {
            JSONObject gp = obj.getJSONObject("gamesPlayed");
            for (String key : gp.keySet()) {
                p.gamesPlayed.put(key, gp.getInt(key));
            }
        }

        // NEW: recent scores
        if (obj.has("recentScores")) {
            JSONObject rs = obj.getJSONObject("recentScores");
            for (String key : rs.keySet()) {
                p.recentScores.put(key, rs.getInt(key));
            }
        }

        // NEW: last played timestamps
        if (obj.has("lastPlayed")) {
            JSONObject lp = obj.getJSONObject("lastPlayed");
            for (String key : lp.keySet()) {
                p.lastPlayed.put(key, lp.getString(key));
            }
        }

        return p;
    }
}