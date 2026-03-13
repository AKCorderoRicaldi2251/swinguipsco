import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class PatternChallengeGUI extends ShootMenu {
    private PatternChallengeDice engine;

    public PatternChallengeGUI(SceneManager manager) {
        // Initialize parent with empty list for manual horizontal placement
        super(manager, List.of());
        this.isHorizontal = true;

        // 1. Initialize the Engine
        this.engine = new PatternChallengeDice(manager);

        // 2. Setup Horizontal Buttons at the bottom
        String[] keys = {"REROLL", "BANK_SCORE", "EXIT"};
        int totalWidth = (keys.length * 150) + ((keys.length - 1) * 20);
        int startX = (900 - totalWidth) / 2;

        for (String key : keys) {
            targets.add(new Target(startX, 530, 150, 45, key));
            startX += 150 + 20;
        }
    }

    @Override
    public void updateLogic() {
        super.updateLogic();

        // Detect if a bullet hits a die
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();

            // World position of bullet (already adjusted in your ShootMenu Bullet class)
            int bx = b.x;
            int by = b.y;

            // Hitbox Math: Must match paintComponent exactly
            int diceStartX = (900 - (5 * 110)) / 2;
            for (int i = 0; i < 5; i++) {
                int dx = diceStartX + (i * 110);
                int dy = 230;

                // Check if bullet is inside the 100x100 die box
                if (bx >= dx && bx <= dx + 100 && by >= dy && by <= dy + 100) {
                    engine.toggleKeep(i);
                    it.remove(); // Kill bullet
                    break;
                }
            }
        }
    }

    @Override
    protected void handleSelection(String label) {
        switch (label) {
            case "REROLL" -> engine.reroll();
            case "BANK_SCORE" -> {
                engine.bankScore();
                // Optionally go back to menu after banking, or just stay to play more
                sceneManager.switchScene(new GameMenu(sceneManager));
            }
            case "EXIT" -> sceneManager.switchScene(new GameMenu(sceneManager));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Draws background and horizontal buttons
        Graphics2D g2 = (Graphics2D) g;

        var old = g2.getTransform();
        g2.translate(camX, camY);

        // --- DRAW DICE ---
        int[] dice = engine.getDice();
        boolean[] keeps = engine.getKeep();
        int diceStartX = (900 - (5 * 110)) / 2;

        for (int i = 0; i < 5; i++) {
            int x = diceStartX + (i * 110);
            int y = 230;

            // Die Body
            g2.setColor(keeps[i] ? new Color(0, 80, 80) : new Color(30, 30, 30));
            g2.fillRoundRect(x, y, 100, 100, 15, 15);

            // Neon Border
            g2.setColor(keeps[i] ? Color.CYAN : Color.GREEN);
            g2.setStroke(new BasicStroke(keeps[i] ? 4 : 2));
            g2.drawRoundRect(x, y, 100, 100, 15, 15);

            // Dice Value (Big and Bold)
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 42));
            String val = String.valueOf(dice[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(val, x + (100 - fm.stringWidth(val)) / 2, y + 65);

            // Locked Indicator
            if (keeps[i]) {
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.drawString("[LOCKED]", x + 22, y + 90);
            }


        }

        g2.setTransform(old);

        // --- TOP UI OVERLAY (Static) ---
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.drawString("> PATTERN_CHALLENGE.SYS", 50, 50);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2.drawString("REROLLS: " + engine.getRerolls(), 50, 85);

        // Status message from Engine
        g2.setColor(Color.YELLOW);
        g2.drawString(engine.getStatus(), 50, 115);

        // Live pattern preview
        ScoreResult currentMatch = engine.calculateScore(dice);
        g2.setColor(Color.GREEN);
        g2.drawString("CURRENT: " + currentMatch.name + " (+" + currentMatch.points + ")", 50, 145);
        if (crossHair != null) {
            crossHair.draw(g2);
        }
    }
}