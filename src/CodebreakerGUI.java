import java.awt.*;
import java.util.List;
import javax.swing.Timer;

public class CodebreakerGUI extends ShootMenu {
    private CodebreakerEngine engine;
    private boolean isGameOver = false;

    public CodebreakerGUI(SceneManager manager) {
        // Empty list in super because we manually place horizontal targets below
        super(manager, List.of());
        this.engine = new CodebreakerEngine(manager.getPlayer());

        // --- HORIZONTAL CONTROL BAR ---
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
            default -> engine.addDieToGuess(Integer.parseInt(label));
        }
    }

    private void checkWinState() {
        if (engine.isSolved()) {
            isGameOver = true;
            // Wait 2 seconds so the player can see the "Success" state, then exit
            Timer timer = new Timer(2000, e -> {
                sceneManager.switchScene(new GameMenu(sceneManager));
            });
            timer.setRepeats(false);
            timer.start();
        } else if (engine.getAttempts() >= 10) {
            // Optional: Auto-exit on loss too after a delay
            isGameOver = true;
            new Timer(3000, e -> sceneManager.switchScene(new GameMenu(sceneManager))).start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Custom Background
        g2.setColor(new Color(10, 15, 10));
        g2.fillRect(0, 0, getWidth(), getHeight());

        var old = g2.getTransform();
        g2.translate(camX, camY);

        // --- DRAW DICE HISTORY ---
        List<CodebreakerEngine.GuessHistory> history = engine.getHistory();
        for (int r = 0; r < history.size(); r++) {
            CodebreakerEngine.GuessHistory row = history.get(r);
            for (int i = 0; i < row.values.size(); i++) {
                drawDice(g2, 320 + (i * 60), 40 + (r * 45), row.values.get(i), row.feedback[i]);
            }
        }

        // --- DRAW BUFFER ---
        List<Integer> current = engine.getCurrentGuess();
        for (int i = 0; i < current.size(); i++) {
            drawDice(g2, 320 + (i * 60), 450, current.get(i), -1);
        }

        // --- DRAW BUTTONS ---
        for (Target t : targets) {
            Rectangle box = t.getDrawBox();
            g2.setColor(t.hovered ? Color.CYAN : new Color(0, 80, 0));
            g2.fill(box);
            g2.setColor(Color.WHITE);
            g2.draw(box);
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            g2.drawString(t.label, box.x + 10, box.y + 25);
        }

        g2.setTransform(old);

        // --- UI OVERLAY ---
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.drawString("> CODE_BREAKER.EXE", 50, 50);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2.drawString("ATTEMPTS: " + engine.getAttempts() + "/10", 50, 85);

        if (engine.isSolved()) {
            g2.setColor(Color.GREEN);
            g2.drawString("[!] ACCESS GRANTED - REDIRECTING...", 50, 115);
        } else if (engine.getAttempts() >= 10) {
            g2.setColor(Color.RED);
            g2.drawString("[X] SYSTEM LOCKED - BOOTING TO MENU", 50, 115);
        }

        crossHair.draw(g2);
    }

    private void drawDice(Graphics2D g2, int x, int y, int val, int feedback) {
        g2.setColor(new Color(25, 25, 25));
        g2.fillRoundRect(x, y, 40, 40, 8, 8);

        if (feedback == 2) g2.setColor(Color.GREEN);
        else if (feedback == 1) g2.setColor(Color.YELLOW);
        else g2.setColor(new Color(60, 60, 60));

        g2.setStroke(new BasicStroke(feedback > 0 ? 3 : 1));
        g2.drawRoundRect(x, y, 40, 40, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString(String.valueOf(val), x + 13, y + 28);
    }
}