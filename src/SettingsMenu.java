import java.util.List;

public class SettingsMenu extends ShootMenu {

    public SettingsMenu(SceneManager manager) {
        super(manager, List.of("Change Color", "Back"));
    }

    @Override
    protected void handleSelection(String label) {
        switch (label) {
            case "Back" -> {
                sceneManager.switchScene(new MainMenu(sceneManager));
            }
        }
    }
}
