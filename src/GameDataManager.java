import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class GameDataManager {

    private static final String SAVE_DIR = "saves/";

    static {
        // Ensure the saves directory exists
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdir();
    }

    // SAVE: saves/username.json
    public static void savePlayer(Player player) {
        // 1. Define the directory and filename
        File saveDir = new File("saves");
        if (!saveDir.exists()) {
            saveDir.mkdirs(); // Creates the folder if it's missing
        }

        File saveFile = new File(saveDir, player.getName() + ".json");

        // 2. Write the data
        try (FileWriter file = new FileWriter(saveFile)) {
            file.write(player.toJSON().toString(4));
            System.out.println(">> System Synced to: " + saveFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("CRITICAL_SAVE_FAILURE: " + e.getMessage());
        }
    }

    // LOAD: Finds specific file by name
    public static Player loadPlayer(String name) {
        String path = SAVE_DIR + name + ".json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            return Player.fromJSON(new JSONObject(content));
        } catch (IOException e) {
            System.out.println(">> Profile not found: " + name);
            return new Player(name);
        }
    }

    // LIST ALL NAMES: For your Load Menu and Scoreboard
    public static List<String> getAllSavedNames() {
        List<String> names = new ArrayList<>();
        File folder = new File(SAVE_DIR);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File f : files) {
                // Remove .json to get just the username
                names.add(f.getName().replace(".json", ""));
            }
        }
        return names;
    }
    public static List<Player> getAllPlayersForScoreboard() {
        List<Player> players = new ArrayList<>();
        File folder = new File("saves/"); // Or wherever your JSONs live

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File f : files) {
                try {
                    // Load each player one by one
                    String content = new String(Files.readAllBytes(f.toPath()));
                    JSONObject json = new JSONObject(content);
                    players.add(Player.fromJSON(json));
                } catch (Exception e) {
                    System.out.println("Skipping corrupted file: " + f.getName());
                }
            }
        }


        return players;
    }
}