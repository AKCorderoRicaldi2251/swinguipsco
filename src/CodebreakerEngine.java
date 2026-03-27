import java.util.*;

public class CodebreakerEngine {
    private int[] secretCode = new int[4];
    private List<Integer> currentGuess = new ArrayList<>();
    private List<GuessHistory> history = new ArrayList<>();

    private int attempts = 0;
    private final int maxAttempts = 6;
    private boolean solved = false;
    private Player player;

    public CodebreakerEngine(Player player) {
        this.player = player;
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            secretCode[i] = rand.nextInt(6) + 1; // Dice 1-6
        }
    }

    public void addDieToGuess(int value) {
        if (currentGuess.size() < 4) currentGuess.add(value);
    }

    public void clearCurrent() {
        currentGuess.clear();
    }

    public void submitGuess() {
        if (currentGuess.size() != 4 || solved) return;

        attempts++;
        int[] guessArr = currentGuess.stream().mapToInt(i -> i).toArray();
        int[] feedback = calculateFeedback(guessArr);

        history.add(new GuessHistory(new ArrayList<>(currentGuess), feedback));

        int hits = 0;
        for (int f : feedback) if (f == 2) hits++;

        if (hits == 4) {
            solved = true;
            saveScore();
        }
        currentGuess.clear();
    }

    private int[] calculateFeedback(int[] guess) {
        int[] result = new int[4];
        boolean[] secretUsed = new boolean[4];
        boolean[] guessUsed = new boolean[4];

        // Hits: Correct number, Correct spot (Value 2)
        for (int i = 0; i < 4; i++) {
            if (guess[i] == secretCode[i]) {
                result[i] = 2;
                secretUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // Blows: Correct number, Wrong spot (Value 1)
        for (int i = 0; i < 4; i++) {
            if (!guessUsed[i]) {
                for (int j = 0; j < 4; j++) {
                    if (!secretUsed[j] && guess[i] == secretCode[j]) {
                        result[i] = 1;
                        secretUsed[j] = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void saveScore() {
        int points = (maxAttempts - attempts + 1) * 25;
        player.setScore("Codebreaker_TotalPoints", player.getScore("Codebreaker_TotalPoints") + points);
        player.recordGamePlayed("Codebreaker", points);
        GameDataManager.savePlayer(player);
    }

    public List<GuessHistory> getHistory() { return history; }
    public List<Integer> getCurrentGuess() { return currentGuess; }
    public int getAttempts() { return attempts; }
    public boolean isSolved() { return solved; }

    public static class GuessHistory {
        public List<Integer> values;
        public int[] feedback;
        public GuessHistory(List<Integer> v, int[] f) { values = v; feedback = f; }
    }
}
