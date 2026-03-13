import java.awt.*;
import java.util.List;

public class DiceGridGUI extends ShootMenu {
    private DiceGridEngine engine;
    private boolean gameOver = false;
    private int finalScore = 0;

    public DiceGridGUI(SceneManager manager) {
        super(manager, List.of("RESET", "EXIT"));
        this.engine = new DiceGridEngine(manager.getPlayer());
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

        // --- DRAW GRID ---
        int[][] grid = engine.getGrid();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int x = 300 + (c * 110);
                int y = 150 + (r * 110);

                g2.setColor(new Color(40, 40, 40));
                g2.fillRoundRect(x, y, 100, 100, 10, 10);
                g2.setColor(Color.CYAN);
                g2.drawRoundRect(x, y, 100, 100, 10, 10);

                if (grid[r][c] != 0) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 40));
                    g2.drawString(String.valueOf(grid[r][c]), x + 35, y + 65);
                }
            }
        }
        g2.setTransform(old);

        // --- UI ---
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.drawString("> DICE_GRID_PUZZLE", 50, 50);

        g2.setColor(Color.WHITE);
        g2.drawString("CURRENT ROLL: " + engine.getCurrentRoll(), 50, 100);

        if (gameOver) {
            g2.setColor(Color.GREEN);
            g2.drawString("FINAL SCORE: " + finalScore, 50, 150);
        } else {
            g2.setColor(Color.YELLOW);
            g2.drawString("STATUS: " + engine.getStatus(), 50, 150);
        }
    }
}