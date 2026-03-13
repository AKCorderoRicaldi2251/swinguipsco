import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class PatternChallengeGUI extends ShootMenu {
    private PatternChallengeDice engine;

    public PatternChallengeGUI(SceneManager manager) {
        super(manager, List.of("REROLL", "BANK_SCORE", "EXIT"));
        this.engine = new PatternChallengeDice(manager);
    }

    @Override
    public void updateLogic() {
        super.updateLogic(); // Handles camera, bullet movement, and menu targets

        // Detect if a bullet hits a die
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();

            // Convert bullet screen position to world position (to match dice)
            int worldX = (int) (b.x - camX);
            int worldY = (int) (b.y - camY);

            for (int i = 0; i < 5; i++) {
                int dx = 100 + (i * 125);
                int dy = 250;

                // If bullet hitbox hits the die box
                if (worldX >= dx && worldX <= dx + 100 && worldY >= dy && worldY <= dy + 100) {
                    engine.toggleKeep(i);
                    it.remove(); // Destroy bullet on hit
                    break;
                }
            }
        }
    }

    @Override
    protected void handleSelection(String label) {
        switch (label) {
            case "REROLL" -> engine.reroll();
            case "BANK_SCORE" -> engine.bankScore();
            case "EXIT" -> sceneManager.switchScene(new GameMenu(sceneManager));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Parent handles background, camera, and targets
        Graphics2D g2 = (Graphics2D) g;

        // Apply camera translate for the dice
        var oldTransform = g2.getTransform();
        g2.translate(camX, camY);

        int[] dice = engine.getDice();
        boolean[] keep = engine.getKeep();

        for (int i = 0; i < 5; i++) {
            int x = 100 + (i * 125);
            int y = 250;

            Color boxColor = keep[i] ? new Color(0, 60, 0) : new Color(30, 30, 30);
            Color borderColor = keep[i] ? Color.GREEN : Color.CYAN;

            g2.setColor(boxColor);
            g2.fillRoundRect(x, y, 100, 100, 15, 15);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x, y, 100, 100, 15, 15);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 45));
            g2.drawString(String.valueOf(dice[i]), x + 35, y + 65);
        }

        g2.setTransform(oldTransform); // Reset for UI text

        // UI Overlay
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.drawString("> PATTERN_CHALLENGE.EXE", 50, 50);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2.drawString("SESSION: " + engine.getSessionScore(), 50, 85);
        g2.setColor(Color.GREEN);
        g2.drawString("LOG: " + engine.getStatus(), 50, 115);
    }
}