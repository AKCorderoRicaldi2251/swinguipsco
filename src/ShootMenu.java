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
        int y = 150;
        for (String opt : options) {
            // Calculate width: at least 200px, or 15px per letter
            int dynamicWidth = Math.max(200, opt.length() * 15);

            // Center the BOX on the screen (900px wide)
            int centerX = (900 - dynamicWidth) / 2;

            targets.add(new Target(centerX, y, dynamicWidth, 45, opt));
            y += 65;
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
        super.paintComponent(g); // Draws the base background
        Graphics2D g2 = (Graphics2D) g;

        // 1. Save the original state (Static Screen Space)
        var oldTransform = g2.getTransform();

        // 2. APPLY THE SWAY (World Space)
        // Everything drawn between here and 'setTransform' will move with the camera
        g2.translate(camX, camY);

        // 3. Set your Font
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();

        for (Target t : targets) {
            Rectangle box = t.getDrawBox();

            // DRAW THE BOX
            g2.setColor(t.hovered ? Color.CYAN : Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(box.x, box.y, box.width, box.height);

            // DRAW THE TEXT (Centered inside the moving box)
            String label = t.label;
            int textWidth = fm.stringWidth(label);

            // Math: Box start + half of the remaining space
            int tx = box.x + (box.width - textWidth) / 2;
            int ty = box.y + (box.height + fm.getAscent()) / 2 - 4;

            g2.drawString(label, tx, ty);
        }

        // Draw the bullets here too so they sway
        g2.setColor(Color.YELLOW);
        for (Bullet b : bullets) g2.fillRect(b.x, b.y, 4, 4);

        // 4. RESTORE THE STATE (Back to Static Screen Space)
        g2.setTransform(oldTransform);

        // 5. DRAW CROSSHAIR (Should NOT sway, stays locked to mouse)
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
