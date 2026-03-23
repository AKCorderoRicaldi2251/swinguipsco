import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ShootMenu extends JPanel {

    protected float camX = 0;
    protected float camY = 0;
    protected boolean isHorizontal = false;
    protected final SceneManager sceneManager;
    protected boolean transitionActive = false;

    protected final CrossHair crossHair = new CrossHair();
    protected final ArrayList<Target> targets = new ArrayList<>();
    protected final ArrayList<Bullet> bullets = new ArrayList<>();
    private String targetToSwitch = null;

    public ShootMenu(SceneManager manager, List<String> options) {
        this.sceneManager = manager;

        int y = 150;
        for (String opt : options) {
            int dynamicWidth = Math.max(200, opt.length() * 15);
            int centerX = (900 - dynamicWidth) / 2;
            targets.add(new Target(centerX, y, dynamicWidth, 45, opt));
            y += 65;
        }
    }

    public void updateLogic() {

        float targetCamX = (crossHair.x - 450) / 10f; // Slightly slower drift
        float targetCamY = (crossHair.y - 300) / 10f;

        // Tight clamp ensures buttons never slide too far for the mouse to reach
        if (targetCamX > 20) targetCamX = 20;
        if (targetCamX < -20) targetCamX = -20;
        if (targetCamY > 15) targetCamY = 15;
        if (targetCamY < -15) targetCamY = -15;

        camX += (targetCamX - camX) * 0.1f;
        camY += (targetCamY - camY) * 0.1f;

        // 2. World Coordinates (Where the buttons actually "live")
        int worldMouseX = crossHair.x - (int)camX;
        int worldMouseY = crossHair.y - (int)camY;

        // 3. Update bullets and check collisions
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update();

            boolean bulletRemoved = false;

            // Check Target Hits
            for (Target t : targets) {
                if (b.getHitbox().intersects(t.hitbox)) {
                    if (!transitionActive) {
                        targetToSwitch = t.label;
                        it.remove(); // Remove bullet
                        bulletRemoved = true;
                        break; // Move to next bullet
                    }
                }
            }

            // Only check if it's dead if it wasn't already removed by a hit
            if (!bulletRemoved && b.isDead()) {
                it.remove();
            }
        }

        // 4. Update hover state (Using world coordinates)
        // This makes the glow follow the button, not the mouse cursor
        for (Target t : targets) {
            t.hovered = t.hitbox.contains(worldMouseX, worldMouseY);
        }

        // 5. Handle Scene Switching
        if (targetToSwitch != null) {
            onTargetHit(targetToSwitch);
            targetToSwitch = null;
        }
    }

    protected void onTargetHit(String label) {
        if (transitionActive) return;
        transitionActive = true;
        handleSelection(label);
        new Timer(500, e -> transitionActive = false).start();
    }

    protected abstract void handleSelection(String label);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.fillRect(0, 0, 900, 600);

        var old = g2.getTransform();
        g2.translate(camX, camY); // START SWAY

        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();

        for (Target t : targets) {
            Rectangle box = t.getDrawBox();

            // Draw Box
            g2.setColor(t.hovered ? Color.CYAN : Color.GREEN);
            g2.setStroke(new BasicStroke(2));
            g2.draw(box);

            // Draw Centered Text
            int textWidth = fm.stringWidth(t.label);
            int tx = box.x + (box.width - textWidth) / 2;
            int ty = box.y + (box.height + fm.getAscent()) / 2 - 4;

            g2.setColor(Color.WHITE);
            g2.drawString(t.label, tx, ty);
        }
        g2.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g2.fillOval(b.x - 3, b.y - 5, 6, 10);
        }

        g2.setTransform(old); // END SWAY
        crossHair.draw(g2);   // Crosshair stays static
    }

    // Inner Classes
    protected class Target {
        Rectangle hitbox;
        String label;
        boolean hovered = false;

        Target(int x, int y, int w, int h, String label) {
            this.hitbox = new Rectangle(x, y, w, h);
            this.label = label;
        }

        Rectangle getDrawBox() {
            if (!hovered) return hitbox;
            int grow = 6; // Reduced grow so it doesn't overlap other buttons
            return new Rectangle(hitbox.x - grow / 2, hitbox.y - grow / 2,
                    hitbox.width + grow, hitbox.height + grow);
        }
    }

    protected class Bullet {
        int x, y;
        int lifetime = 20;
        Bullet(int x, int y) {
            // Save the world coordinates at the moment of firing
            this.x = x - (int)camX;
            this.y = y - (int)camY;
        }
        void update() { lifetime--; }
        boolean isDead() { return lifetime <= 0; }
        Rectangle getHitbox() { return new Rectangle(x, y, 6, 10); }
    }

    protected static class CrossHair {
        int x, y;
        void updatePosition(int mouseX, int mouseY) { x = mouseX; y = mouseY; }
        void draw(Graphics2D g2) {
            g2.setColor(Color.RED);
            g2.drawOval(x - 8, y - 8, 16, 16);
            g2.drawLine(x - 12, y, x + 12, y);
            g2.drawLine(x, y - 12, x, y + 12);
        }
    }
}