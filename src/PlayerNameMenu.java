import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlayerNameMenu extends ShootMenu {


    public PlayerNameMenu(SceneManager manager) {
        super(manager, List.of("New Game","Load Game","Quit")); // just one target for now
    }

    @Override
    protected void handleSelection(String label) {

        switch (label) {
            case "New Game" -> {
                String input = JOptionPane.showInputDialog("Register New Username:");
                if (input != null && !input.trim().isEmpty()) {
                    sceneManager.setPlayer(new Player(input.trim()));
                    sceneManager.switchScene(new MainMenu(sceneManager));
                }
            }

            case "Load Game" -> {
                List<String> savedNames = GameDataManager.getAllSavedNames();
                if (savedNames.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "NO DATA FOUND.");
                    return;
                }

                String selected = (String) JOptionPane.showInputDialog(
                        this, "SELECT_PROFILE:", "TERMINAL_LOAD",
                        JOptionPane.QUESTION_MESSAGE, null,
                        savedNames.toArray(), savedNames.get(0));

                if (selected != null) {
                    sceneManager.setPlayer(GameDataManager.loadPlayer(selected));
                    sceneManager.switchScene(new MainMenu(sceneManager));
                }
            }
            case "Quit" -> System.exit(0);

        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(0, 50, 0)); // Dark green
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.drawString("SECURE_BOOT_v2.6...", 20, 20);
        g2.drawString("CHECKING_DAT_FILES...", 20, 35);
    }
}