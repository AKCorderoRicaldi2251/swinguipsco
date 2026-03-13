import java.awt.*;
import java.util.List;
import javax.swing.Timer;

public class CodebreakerGUI extends ShootMenu {
    private CodebreakerEngine engine; // Must be initialized!
    private boolean isGameOver = false;

    public CodebreakerGUI(SceneManager manager) {
        super(manager, List.of());
        this.isHorizontal = true;

        // FIX 1: Initialize the engine
        this.engine = new CodebreakerEngine(manager.getPlayer());

        // horizontal layout
        String[] keys = {"1", "2", "3", "4", "5", "6", "SUBMIT", "CLEAR", "EXIT"};
        int startX = 50;
        for (String key : keys) {
            int width = (key.length() > 1) ? 90 : 50;
            targets.add(new Target(startX, 530, width, 40, key));
            startX += width + 10;
        }
    }

    @Override
    protected void handleSelection(String label) {
        if (isGameOver && !label.equals("EXIT")) return;

        switch (label) {
            case "SUBMIT" -> {
                engine.submitGuess();
                checkWinState();
            }
            case "CLEAR" -> engine.clearCurrent();
            case "EXIT" -> sceneManager.switchScene(new GameMenu(sceneManager));
            default -> {
                // Wrap in try-catch just in case a non-number button is hit
                try {
                    engine.addDieToGuess(Integer.parseInt(label));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input: " + label);
                }
            }
        }
    }

    private void checkWinState() {
        if (engine.isSolved()) {
            isGameOver = true;
            Timer timer = new Timer(2000, e -> sceneManager.switchScene(new GameMenu(sceneManager)));
            timer.setRepeats(false);
            timer.start();
        } else if (engine.getAttempts() >= 10) {
            isGameOver = true;
            new Timer(3000, e -> sceneManager.switchScene(new GameMenu(sceneManager))).start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Draws background and horizontal buttons
        Graphics2D g2 = (Graphics2D) g;

        var old = g2.getTransform();
        g2.translate(camX, camY);

        // --- DRAW DICE HISTORY ---
        List<CodebreakerEngine.GuessHistory> history = engine.getHistory();
        int historyStartX = (900 - (4 * 55)) / 2;

        for (int r = 0; r < history.size(); r++) {
            var row = history.get(r);
            for (int i = 0; i < row.values.size(); i++) {
                int x = historyStartX + (i * 55);
                int y = 40 + (r * 45);
                // FIX 2: Use the drawDice method so we can see numbers and borders!
                drawDice(g2, x, y, row.values.get(i), row.feedback[i]);
            }
        }

        // --- DRAW CURRENT BUFFER ---
        List<Integer> current = engine.getCurrentGuess();
        for (int i = 0; i < current.size(); i++) {
            int x = historyStartX + (i * 55);
            // Draw current selection as dice without feedback borders
            drawDice(g2, x, 460, current.get(i), -1);
        }

        g2.setTransform(old);

        // --- UI TEXT ---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.drawString("ATTEMPTS: " + engine.getAttempts() + "/10", 50, 85);

        if (engine.isSolved()) {
            g2.setColor(Color.GREEN);
            g2.drawString("ACCESS GRANTED", 50, 110);
        }
        if (crossHair != null) {
            crossHair.draw(g2);
        }
    }

    private void drawDice(Graphics2D g2, int x, int y, int val, int feedback) {
        // Body
        g2.setColor(new Color(25, 25, 25));
        g2.fillRoundRect(x, y, 40, 40, 8, 8);

        // Feedback Border (2 = Green/Hit, 1 = Yellow/Blow)
        if (feedback == 2) g2.setColor(Color.GREEN);
        else if (feedback == 1) g2.setColor(Color.YELLOW);
        else g2.setColor(new Color(60, 60, 60));

        g2.setStroke(new BasicStroke(feedback > 0 ? 3 : 1));
        g2.drawRoundRect(x, y, 40, 40, 8, 8);

        // Number
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString(String.valueOf(val), x + 13, y + 28);
    }
}