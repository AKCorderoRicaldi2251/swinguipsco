import java.awt.*;
import java.util.List;

public class DiceGridGUI extends ShootMenu {
    private DiceGridEngine engine;
    private boolean gameOver = false;
    private int finalScore = 0;

    public DiceGridGUI(SceneManager manager) {
        super(manager, List.of());
        this.isHorizontal = true;
        this.engine = new DiceGridEngine(manager.getPlayer());

        // Horizontal Buttons
        String[] keys = {"RESET", "EXIT"};
        int startX = (900 - 220) / 2; // (2 buttons * 100) + 20 gap
        for (String key : keys) {
            targets.add(new Target(startX, 530, 100, 40, key));
            startX += 100 + 20;
        }
    }

    @Override
    public void updateLogic() {
        super.updateLogic();

        // Use the same bullet-collision logic as Game 1
        var it = bullets.iterator();
        while (it.hasNext()) {
            var b = it.next();
            int worldX = (int) (b.x - camX);
            int worldY = (int) (b.y - camY);

            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    int x = 300 + (c * 110);
                    int y = 150 + (r * 110);

                    if (worldX >= x && worldX <= x + 100 && worldY >= y && worldY <= y + 100) {
                        if (!gameOver && engine.placeDie(r, c)) {
                            if (engine.isFull()) {
                                finalScore = engine.calculateScore();
                                gameOver = true;
                            }
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void handleSelection(String label) {
        if (label.equals("RESET")) {
            this.engine = new DiceGridEngine(sceneManager.getPlayer());
            gameOver = false;
        } else if (label.equals("EXIT")) {
            sceneManager.switchScene(new GameMenu(sceneManager));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        var old = g2.getTransform();
        g2.translate(camX, camY);

        // --- DRAW 3x3 GRID (Centered) ---
        int gridWidth = (3 * 100) + (2 * 10);
        int startX = (900 - gridWidth) / 2;
        int startY = 150;

        int[][] grid = engine.getGrid();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int x = startX + (c * 110);
                int y = startY + (r * 110);
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(x, y, 100, 100, 5, 5);
                g2.setColor(Color.CYAN);
                g2.drawRoundRect(x, y, 100, 100, 5, 5);

                if (grid[r][c] != 0) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Monospaced", Font.BOLD, 30));
                    g2.drawString(String.valueOf(grid[r][c]), x + 40, y + 60);
                }
            }
        }
        g2.setTransform(old);

        if (!gameOver) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.drawString("CURRENT DIE:", 50, 100);

            // Draw a special box for the "Next" die
            g2.setColor(Color.YELLOW);
            g2.drawRoundRect(50, 110, 60, 60, 10, 10);
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            // Assuming your engine has a getNextDie() method
            g2.drawString(String.valueOf(engine.getCurrentRoll()), 68, 152);
        } else {
            g2.setColor(Color.CYAN);
            g2.setFont(new Font("Monospaced", Font.BOLD, 24));
            g2.drawString("GRID FULL! FINAL SCORE: " + finalScore, 50, 100);
        }

        if (crossHair != null) {
            crossHair.draw(g2);
        }
    }
}