import java.util.List;
import javax.swing.*;
import java.awt.*;

public class GameMenu extends ShootMenu {

    public GameMenu(SceneManager manager) {
        // Updated to match your list precisely
        super(manager, List.of("Game 1", "Game 2", "Game 3", "Game 4", "Load Leaderboard", "Back"));
    }

    @Override
    protected void handleSelection(String label) {
        switch (label) {
            case "Game 1" -> sceneManager.switchScene(new PatternChallengeGUI(sceneManager));
            case "Game 2" -> sceneManager.switchScene(new DiceGridGUI(sceneManager));
            case "Game 3" -> sceneManager.switchScene(new CodebreakerGUI(sceneManager));
            case "Game 4" -> {
                sceneManager.switchScene(new SystemCrashGUI(sceneManager));
            }

            case "Load Leaderboard" -> {
                showScoreboard();
            }

            case "Back" -> {
                sceneManager.switchScene(new MainMenu(sceneManager));
            }

            // You can add Game 1, 2, and 3 here later!
            default -> System.out.println("System Note: " + label + " is not yet implemented.");
        }
    }

    private void showScoreboard() {
        List<Player> allPlayers = GameDataManager.getAllPlayersForScoreboard();

        StringBuilder sb = new StringBuilder();
        sb.append("=== [ GLOBAL_SYSTEM_RANKINGS ] ===\n\n");

        // ── Summary table (one row per player) ──────────────────────────────
        sb.append(String.format("%-15s | %-8s | %-10s | %-10s | %-10s | %-10s\n",
                "HACKER_ID", "GAMES", "G1_HIGH", "G2_HIGH", "G3_HIGH", "SC W/L"));
        sb.append("-".repeat(80)).append("\n");

        if (allPlayers.isEmpty()) {
            sb.append(">> NO DATA DETECTED IN ROOT/SAVES/\n");
        } else {
            for (Player p : allPlayers) {
                String crash = p.getScore("SystemCrash_Wins") + "/" + p.getScore("SystemCrash_Losses");
                sb.append(String.format("%-15s | %-8d | %-10d | %-10d | %-10d | %-10s\n",
                        p.getName(),
                        p.getTotalGamesPlayed(),
                        p.getScore("PatternChallenge_HighScore"),
                        p.getScore("DiceGrid_HighScore"),
                        p.getScore("Codebreaker_HighScore"),
                        crash
                ));
            }
        }

        // ── Per-game detail block for each player ───────────────────────────
        sb.append("\n\n=== [ PER_PLAYER_DETAIL ] ===\n");

        String[] gameKeys   = { "PatternChallenge", "DiceGrid", "Codebreaker", "SystemCrash" };
        String[] gameLabels = { "G1 Pattern Challenge", "G2 Dice Grid", "G3 Codebreaker", "G4 System Crash" };

        for (Player p : allPlayers) {
            sb.append("\n>> ").append(p.getName())
                    .append("  (total sessions: ").append(p.getTotalGamesPlayed()).append(")\n");

            for (int i = 0; i < gameKeys.length; i++) {
                String key = gameKeys[i];
                sb.append(String.format(
                        "   %-22s | played: %-4d | high: %-6d | recent: %-6d | last: %s\n",
                        gameLabels[i],
                        p.getGamesPlayed(key),
                        p.getScore(key + "_HighScore"),
                        p.getRecentScore(key),
                        p.getLastPlayed(key)
                ));
            }
        }

        // Display in a "Terminal-style" ScrollPane
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(new Color(10, 10, 10)); // Slightly darker for contrast
        textArea.setForeground(Color.GREEN);
        textArea.setCaretColor(Color.GREEN);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(650, 400));
        scroll.setBorder(BorderFactory.createLineBorder(Color.GREEN));

        JOptionPane.showMessageDialog(this, scroll, "GLOBAL_RANKINGS_v2.0", JOptionPane.PLAIN_MESSAGE);
    }
}