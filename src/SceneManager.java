import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SceneManager extends JPanel {

    private ShootMenu currentScene;
    private ShootMenu nextScene;

    private float alpha = 0f;
    private boolean fadingOut = false;
    private Timer fadeTimer;
    private Player player; // reference to the current player

    public void setPlayer(Player p) {
        this.player = p;
    }

    public Player getPlayer() {
        return player;
    }

    public SceneManager() {
        setPreferredSize(new Dimension(900, 600));
        setFocusable(true);
        requestFocusInWindow();

        // centralized mouse input
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentScene != null) currentScene.crossHair.updatePosition(e.getX(), e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentScene != null)
                    currentScene.bullets.add(currentScene.new Bullet(currentScene.crossHair.x, currentScene.crossHair.y));
            }
        });

        // main loop ~60 FPS
        new Timer(16, e -> {
            if (currentScene != null) currentScene.updateLogic();
            repaint();
        }).start();
    }

    // safely set the initial scene
    public void setInitialScene(ShootMenu scene) {
        this.currentScene = scene;
    }

    // trigger fade + switch
    public void switchScene(ShootMenu newScene) {

        if (fadeTimer != null) {
            fadeTimer.stop();
        }

        this.nextScene = newScene;
        fadingOut = true;
        alpha = 0f;

        fadeTimer = new Timer(16, e -> {
            if (fadingOut) {
                alpha += 0.05f;
                if (alpha >= 1f) {
                    alpha = 1f;
                    this.currentScene = nextScene;
                    this.fadingOut = false; // start fade in

                    this.revalidate();
                    this.repaint();
                }
            } else {
                alpha -= 0.05f;
                if (alpha <= 0f) {
                    alpha = 0f;
                    fadeTimer.stop();
                }
            }
        });

        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw the current scene
        if (currentScene != null) currentScene.paintComponent((Graphics2D) g);

        // draw fade overlay on top
        if (alpha > 0f) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
