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
        // Retrieve all player files from the /saves/ folder
        List<Player> allPlayers = GameDataManager.getAllPlayersForScoreboard();

        StringBuilder sb = new StringBuilder();
        sb.append("--- [ GLOBAL_SYSTEM_RANKINGS ] ---\n\n");

        // Updated Header: Hacker ID | System Crash (W/L) | Game 1 High | Game 2 High | Game 3 Best
        sb.append(String.format("%-15s | %-12s | %-8s | %-8s | %-8s\n",
                "HACKER_ID", "CRASH (W/L)", "G1_PAT", "G2_GRID", "G3_CODE"));
        sb.append("--------------------------------------------------------------------------\n");

        if (allPlayers.isEmpty()) {
            sb.append(">> NO DATA DETECTED IN ROOT/SAVES/");
        } else {
            // Sort players by total wins or high score if you like
            for (Player p : allPlayers) {
                // Format System Crash as "Wins/Losses" (e.g., 10/2)
                String crashStats = p.getScore("SystemCrash_Wins") + "/" + p.getScore("SystemCrash_Losses");

                sb.append(String.format("%-15s | %-12s | %-8d | %-8d | %-8d\n",
                        p.getName(),
                        crashStats,
                        p.getScore("Game1_Total"),      // Patterns Challenge
                        p.getScore("DiceGrid_HighScore"), // Dice Grid
                        p.getScore("Codebreaker_MinAttempts") // Codebreaker (Attempts)
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