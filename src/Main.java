import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Dice Shooter Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            frame.setResizable(false);

            SceneManager sceneManager = new SceneManager();
            PlayerNameMenu nameScene = new PlayerNameMenu(sceneManager);
            sceneManager.setInitialScene(nameScene);


            frame.add(sceneManager);
            frame.pack();
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }
}
