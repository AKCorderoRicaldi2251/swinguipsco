import java.awt.*;
import java.util.List;
import javax.swing.*;

public class SystemCrashGUI extends ShootMenu {

    private SystemCrashDice engine;
    private enum State { PLAYING, WIN_CUTSCENE, LOSE_CUTSCENE }
    private State currentState = State.PLAYING;

    private float screenShake = 0;
    private boolean gunshotEffectTriggered = false;
    private boolean targetsInitialized = false;

    public SystemCrashGUI(SceneManager manager) {

        super(manager, java.util.Arrays.asList());
        this.isHorizontal = true; // Use the horizontal level style
        this.engine = new SystemCrashDice(manager);

        // --- CENTERED HORIZONTAL BUTTONS ---
        String[] keys = {"Bid", "Liar", "EXIT"};
        int totalWidth = (keys.length * 140) + ((keys.length - 1) * 20);
        int startX = (900 - totalWidth) / 2;

        for (String key : keys) {
            targets.add(new Target(startX, 470, 140, 60, key));
            startX += 140 + 20;
        }

    }

    @Override
    protected void handleSelection(String label) {
        // 1. Prevent double-triggering if already fading
        System.out.println("Hit: " + label);

        if (label.equals("REBOOT_TRIGGER") || label.equals("DAVY_CORE")) {
            screenShake = 60.0f;
            System.out.println("SWITCHING TO MENU...");
            transitionActive = false;

            if (sceneManager == null) {
                System.out.println("ERROR: sceneManager is NULL!");
                return;
            }// Reset this just in case!
            sceneManager.switchScene(new MainMenu(sceneManager));
            return;
        }

        if (engine.isRoundOver()) {
            engine.startNewRound();
            return;
        }

        switch (label) {
            case "Bid" -> openBidDialog();
            case "Liar" -> engine.playerCallLiar();
            case "EXIT" -> sceneManager.switchScene(new GameMenu(sceneManager));
        }
    }

    private void openBidDialog() {
        int face ;
        int qty ;

        // 1. GET FACE VALUE (Loop until valid)
        while (true) {
            String faceStr = JOptionPane.showInputDialog(this,
                    "DICE: " + engine.getPlayerDice() + "\nFACE VALUE (2-6):",
                    "TERMINAL_INPUT", JOptionPane.QUESTION_MESSAGE);

            if (faceStr == null) return;

            try {
                face = Integer.parseInt(faceStr.trim());
                if (face >= 2 && face <= 6) break; // Valid!
                JOptionPane.showMessageDialog(this, "SYSTEM ERROR: Face must be 2-6 (1 is a BUG).");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "INVALID PACKET: Enter a number.");
            }
        }

        // 2. GET QUANTITY (Loop until valid)
        while (true) {
            String qtyStr = JOptionPane.showInputDialog(this,
                    "CURRENT BID: " + engine.getCurrentBidQuantity() + "x " + engine.getActiveDiceValue() +
                            "\nENTER QUANTITY:", "TERMINAL_INPUT", JOptionPane.QUESTION_MESSAGE);

            if (qtyStr == null) return;

            try {
                qty = Integer.parseInt(qtyStr.trim());

                // LOGIC: Increase Qty OR stay same Qty but increase Face
                boolean higherQty = qty > engine.getCurrentBidQuantity();
                boolean sameQtyHigherFace = (qty == engine.getCurrentBidQuantity() && face > engine.getActiveDiceValue());

                if (higherQty || sameQtyHigherFace) {
                    break; // Valid bid!
                }

                JOptionPane.showMessageDialog(this, "INSUFFICIENT DATA: Must raise Quantity or Face Value.");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "INVALID PACKET: Enter a number.");
            }
        }

        // 3. EXECUTE
        engine.playerBid(face, qty);

        Timer davyDelay = new Timer(1500, e -> {
            engine.computerTurn();
            ((Timer)e.getSource()).stop();
        });
        davyDelay.start();
    }

    @Override
    public void updateLogic() {

        super.updateLogic(); // This handles the "Bid" and "Liar" clicks automatically

        // Only switch targets if we haven't already
        if (currentState == State.PLAYING) {
            if (engine.getPlayerDiceCount() <= 0) {
                currentState = State.LOSE_CUTSCENE;
                targets.clear();
                targets.add(new Target(0, 0, 900, 600, "REBOOT_TRIGGER"));
            } else if (engine.getDealerDiceCount() <= 0) {
                currentState = State.WIN_CUTSCENE;
                targets.clear();
                targets.add(new Target(350, 150, 200, 200, "DAVY_CORE"));
            }
        }

        if (screenShake > 0) screenShake *= 0.9f;

        // 2. Screen Shake decay
        if (screenShake > 0) {
            screenShake *= 0.9f;
            if (screenShake < 0.1f) screenShake = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 1. Clear the panel (Don't call super.paintComponent(g) twice)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) g;
        var screenSpaceTransform = g2.getTransform(); // Save the "Perfect" screen position

        // 2. APPLY PARALLAX (The smooth drift)
        g2.translate(camX, camY);

        // 3. APPLY SHAKE (The jitter)
        // We do this AFTER camX/Y so the shake moves the world relative to your eyes
        if (screenShake > 0) {
            g2.translate((Math.random() * screenShake) - screenShake/2,
                    (Math.random() * screenShake) - screenShake/2);
        }

        // 4. DRAW THE CURRENT STATE
        if (currentState == State.LOSE_CUTSCENE) {
            renderLoseScene(g2);
        } else if (currentState == State.WIN_CUTSCENE) {
            renderWinScene(g2);
        } else {
            // We manually draw the ShootMenu targets because we aren't calling super.paintComponent
            for (Target t : targets) {
                g2.setColor(t.hovered ? Color.ORANGE : Color.WHITE);
                g2.drawRect(t.hitbox.x, t.hitbox.y, t.hitbox.width, t.hitbox.height);
                g2.drawString(t.label, t.hitbox.x + 20, t.hitbox.y + 35);
            }
            drawDealerArea(g2);
            drawPlayerDice(g2);
            drawCurrentBid(g2);
            drawGameInfo(g2);
        }

        // 5. RESET TO SCREEN SPACE (For the Crosshair)
        g2.setTransform(screenSpaceTransform);
        if (crossHair != null) {
            crossHair.draw(g2);
        }
    }

    private void renderLoseScene(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (!gunshotEffectTriggered) {
            screenShake = 50; // The "Gunshot" impact
            gunshotEffectTriggered = true;
        }

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2.drawString("CRITICAL_ERROR: SYSTEM_HALT", 280, 300);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(">> CONNECTION TERMINATED. CLICK TO REBOOT. <<", 310, 550);
    }

    private void renderWinScene(Graphics2D g2) {
        g2.setColor(new Color(0, 20, 0));
        g2.fillRect(0, 0, getWidth(), getHeight());

        for (Target t : targets) {
            if (t.label.equals("DAVY_CORE")) { // Access field directly
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(3));

                // Reach into the hitbox Rectangle
                int tx = t.hitbox.x;
                int ty = t.hitbox.y;
                int tw = t.hitbox.width;
                int th = t.hitbox.height;

                // Draw Brackets
                g2.drawLine(tx, ty, tx + 20, ty);
                g2.drawLine(tx, ty, tx, ty + 20);
                g2.drawLine(tx + tw, ty, tx + tw - 20, ty);
                g2.drawLine(tx + tw, ty, tx + tw, ty + 20);
                g2.drawLine(tx, ty + th, tx + 20, ty + th);
                g2.drawLine(tx, ty + th, tx, ty + th - 20);
                g2.drawLine(tx + tw, ty + th, tx + tw - 20, ty + th);
                g2.drawLine(tx + tw, ty + th, tx + tw, ty + th - 20);
            }
        }

        // Corrupted Face
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 50));
        g2.drawString("[ X _ X ]", 365, 265);

        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString("TERMINATE PROCESS?", 315, 450);
    }

    private void drawDealerArea(Graphics2D g2) {
        // 1. The Header Box (Name and Stats)
        g2.setColor(Color.BLACK);
        g2.fillRect(350, 40, 200, 80);
        g2.setColor(new Color(30, 80, 150));
        g2.fillRect(360, 50, 180, 60);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.drawString(" \\ / ", 380, 75);
        g2.drawString(" o-o ", 380, 90);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // 2. THE DICE (Drawn underneath the box)
        List<Integer> compDice = engine.getCompDiceValues();
        int startX = 450 - (compDice.size() * 35) / 2; // Center them under the box
        int y = 140; // Positioned below the header box

        for (int val : compDice) {
            // Die Background
            g2.setColor(new Color(20, 20, 20));
            g2.fillRoundRect(startX, y, 30, 30, 5, 5);

            // Die Border
            g2.setStroke(new BasicStroke(1));
            g2.setColor(engine.isRoundOver() ? Color.CYAN : Color.DARK_GRAY);
            g2.drawRoundRect(startX, y, 30, 30, 5, 5);

            if (engine.isRoundOver()) {
                // REVEALED NUMBERS
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.drawString(String.valueOf(val), startX + 10, y + 22);

                // Highlight Bugs (1s) and Patches (6s)
                if (val == 1) { // Bug
                    g2.setColor(new Color(255, 0, 0, 100));
                    g2.fillRect(startX, y, 30, 30);
                } else if (val == 6) { // Patch
                    g2.setColor(new Color(0, 255, 0, 100));
                    g2.fillRect(startX, y, 30, 30);
                }
            } else {
                // ENCRYPTION EFFECT
                g2.setColor(new Color(0, 255, 0, 80));
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                // Randomly flickering characters look cool here
                String glyph = "#" ;
                g2.drawString(glyph, startX + 11, y + 20);
            }
            startX += 35;
        }
    }

    private void drawPlayerDice(Graphics2D g2) {
        List<Integer> dice = engine.getPlayerDice();
        int x = 200;
        for (int value : dice) {
            g2.setColor(Color.GRAY);
            g2.fillRoundRect(x + 2, 352, 50, 50, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, 350, 50, 50, 10, 10);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString(String.valueOf(value), x + 18, 382);
            x += 70;
        }
    }

    private void drawCurrentBid(Graphics2D g2) {
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String bidText = (engine.getCurrentBidQuantity() == 0) ?
                "WAITING FOR BID..." :
                "BID: " + engine.getCurrentBidQuantity() + " x [" + engine.getActiveDiceValue() + "]s";
        g2.drawString(bidText, 330, 250);
    }

    private void drawGameInfo(Graphics2D g2) {
        Player p = sceneManager.getPlayer();
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.drawString("ID: " + p.getName(), 20, 30);
        g2.drawString("SYS_WINS: " + p.getScore("SystemCrash_Wins"), 20, 50);
        g2.drawString("SYS_LOSS: " + p.getScore("SystemCrash_Losses"), 20, 70);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("Buffer: " + engine.getPlayerDiceCount() + " dice", 50, 520);
        g2.drawString("Jones: " + engine.getDealerDiceCount() + " dice", 50, 540);

        if (engine.getLastResultMessage() != null) {
            g2.setColor(Color.ORANGE);
            g2.drawString(engine.getLastResultMessage(), 280, 320);
            if (engine.isRoundOver()) {
                g2.setColor(Color.GREEN);
                g2.drawString(">> SHOOT ANY BUTTON TO CONTINUE <<", 290, 345);
            }
        }
    }


}
