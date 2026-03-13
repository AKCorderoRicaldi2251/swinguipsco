import java.util.List;

public class MainMenu extends ShootMenu{

    public MainMenu(SceneManager manager) {
        super(manager, List.of("Play", "Settings", "Quit"));
    }

    @Override
    protected void handleSelection(String label) {
        switch (label) {
            case "Play" -> sceneManager.switchScene(new GameMenu(sceneManager));
            case "Settings" -> sceneManager.switchScene(new SettingsMenu(sceneManager));
            case "Quit" -> System.exit(0);
        }
    }
}
