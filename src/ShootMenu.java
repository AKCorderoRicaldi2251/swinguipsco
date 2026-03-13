import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ShootMenu extends JPanel {

    // Camera offset for a little smoothing effect
    protected float camX = 0;
    protected float camY = 0;

    // SceneManager reference for transitions
    protected final SceneManager sceneManager;

    // Prevent multiple transitions at the same time
    protected boolean transitionActive = false;

    // Crosshair, bullets, targets
    protected final CrossHair crossHair = new CrossHair();
    protected final ArrayList<Target> targets = new ArrayList<>();
    protected final ArrayList<Bullet> bullets = new ArrayList<>();
    private String targetToSwitch = null;

    // Game loop timer

    // Constructor: pass SceneManager and menu options
    public ShootMenu(SceneManager manager, List<String> options) {
        this.sceneManager = manager;

        // Add targets dynamically based on options
        int y = 60;
        for (String opt : options) {
            targets.add(new Target(120, y, 160, 40, opt));
            y += 70;
        }


    }

    // Called every frame
    public void updateLogic() {
        int centerX = 450; // panel center
        int centerY = 300;

        // Smooth camera movement based on crosshair
        float targetCamX = (crossHair.x - 450) / 20f;
        float targetCamY = (crossHair.y - 300) / 20f;
        camX += (targetCamX - camX) * 0.1f;
        camY += (targetCamY - camY) * 0.1f;

        // 2. IMPORTANT: Adjust mouse coordinates to "World Space"
        // This ensures shooting and hovering works even when the camera shifts.
        int worldMouseX = (int) (crossHair.x - camX);
        int worldMouseY = (int) (crossHair.y - camY);

        // Update bullets
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update();

            for (Target t : targets) {
                if (b.getHitbox().intersects(t.hitbox)) {
                    if (!transitionActive) {
                        targetToSwitch = t.label;
                        it.remove(); // KILL THE BULLET IMMEDIATELY so it can't hit again
                        break;       // Stop checking other targets for this bullet
                    }
                }
            }

            // Check if it died of old age (only if it wasn't already removed)
            if (b.isDead() && bullets.contains(b)) it.remove();
        }

        // Update hover state for targets
        targets.forEach(t -> t.hovered = t.hitbox.contains(crossHair.x, crossHair.y));

        if (targetToSwitch != null) {
            onTargetHit(targetToSwitch); // triggers handleSelection safely
            targetToSwitch = null;
        }
    }

    // Called when a target is hit
    protected void onTargetHit(String label) {
        if (transitionActive) return; // prevent double triggers
        transitionActive = true;
        handleSelection(label);


        // Simple cooldown before another transition
        new Timer(500, e -> transitionActive = false).start();
    }

    // Must be implemented in every menu
    protected abstract void handleSelection(String label);

    // Called by SceneManager to draw the menu
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        var oldTransform = g2.getTransform();

        // --- Start World Space ---
        g2.translate(camX, camY);

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, 900, 600);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        for (Target t : targets) {
            Rectangle box = t.getDrawBox();
            g2.setColor(t.hovered ? Color.ORANGE : Color.WHITE);
            g2.drawRect(box.x, box.y, box.width, box.height);
            g2.drawString(t.label, box.x + 50, box.y + 25);
        }

        g2.setColor(Color.YELLOW); // Bullets should pop!
        for (Bullet b : bullets) g2.fillRect(b.x, b.y, 4, 4);

        // --- End World Space ---
        g2.setTransform(oldTransform);

        // Draw crosshair in SCREEN space so it stays exactly under the mouse
        crossHair.draw(g2);

    }

    // ===== Inner Classes =====

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
            int grow = 10;
            return new Rectangle(
                    hitbox.x - grow / 2,
                    hitbox.y - grow / 2,
                    hitbox.width + grow,
                    hitbox.height + grow
            );
        }
    }

    protected class Bullet {
        int x, y;
        int lifetime = 20;

        Bullet(int x, int y) { this.x = x; this.y = y; }

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
