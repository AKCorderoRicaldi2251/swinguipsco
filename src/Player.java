import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private final String name;
    private final Map<String, Integer> scores = new HashMap<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // ===== Score management =====
    public void setScore(String game, int score) {
        scores.put(game, score);
    }

    // NEW HELPER: Makes updating wins easier
    public void incrementScore(String game) {
        scores.put(game, getScore(game) + 1);
    }

    public int getScore(String game) {
        return scores.getOrDefault(game, 0);
    }
    // Add this to Player.java
    public void addPoints(String game, int points) {
        int current = getScore(game);
        scores.put(game, current + points);
    }

    public Map<String, Integer> getAllScores() {
        return new HashMap<>(scores);
    }

    // ===== Serialization =====
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("scores", scores);
        return obj;
    }

    public static Player fromJSON(JSONObject obj) {
        String name = obj.getString("name");
        Player p = new Player(name);

        JSONObject scoresJson = obj.getJSONObject("scores");
        for (String game : scoresJson.keySet()) {
            int score = scoresJson.getInt(game);
            p.setScore(game, score);
        }
        return p;
    }
}