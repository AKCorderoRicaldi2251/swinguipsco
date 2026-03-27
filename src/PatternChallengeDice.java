import java.util.*;

public class PatternChallengeDice {
    private int[] dice = new int[5];
    private boolean[] keep = new boolean[5];
    private int rerollsLeft = 2;
    private int totalScore = 0;
    private Player player;
    private String lastStatus = "Roll the dice!";
    private Random rand = new Random();
    private SceneManager manager;

    public PatternChallengeDice(SceneManager manager) {
        this.manager = manager;
        this.player = manager.getPlayer();
        startNewRound();
    }

    public void startNewRound() {
        for (int i = 0; i < 5; i++) {
            dice[i] = rand.nextInt(6) + 1;
            keep[i] = false;
        }
        rerollsLeft = 2;
        lastStatus = "New Round Started";
    }

    public void reroll() {
        if (rerollsLeft > 0) {
            for (int i = 0; i < 5; i++) {
                if (!keep[i]) dice[i] = rand.nextInt(6) + 1;
            }
            rerollsLeft--;
            lastStatus = "Rerolls remaining: " + rerollsLeft;
        } else {
            lastStatus = "OUT OF REROLLS: Bank your score!";
        }
    }

    public void toggleKeep(int index) {
        if (index >= 0 && index < 5) keep[index] = !keep[index];
    }

    public void bankScore() {
        ScoreResult result = calculateScore(dice);
        totalScore += result.points;
        lastStatus = "BANKED: " + result.name + " (+" + result.points + ")";

        // Update the Player's permanent save file
        int currentTotal = player.getScore("Game1_Total");
        player.setScore("Game1_Total", currentTotal + result.points);

        player.recordGamePlayed("PatternChallenge", result.points);


        // Save to JSON via your Manager
        GameDataManager.savePlayer(player);

        // Reset for next round
        startNewRound();
    }

    public ScoreResult calculateScore(int[] dice) {
        int[] counts = new int[7];
        for (int d : dice) counts[d]++;

        List<Integer> freq = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            if (counts[i] > 0) freq.add(counts[i]);
        }
        freq.sort(Collections.reverseOrder());

        if (freq.equals(Arrays.asList(5))) return new ScoreResult("Five of a Kind", 50);
        if (freq.equals(Arrays.asList(4, 1))) return new ScoreResult("Four of a Kind", 40);
        if (freq.equals(Arrays.asList(3, 2))) return new ScoreResult("Full House", 35);
        if (isStraight(counts)) return new ScoreResult("Straight", 30);
        if (freq.equals(Arrays.asList(3, 1, 1))) return new ScoreResult("Three of a Kind", 25);
        if (freq.equals(Arrays.asList(2, 2, 1))) return new ScoreResult("Two Pairs", 20);
        if (freq.equals(Arrays.asList(2, 1, 1, 1))) return new ScoreResult("One Pair", 10);

        return new ScoreResult("No Pattern", 0);
    }

    private boolean isStraight(int[] counts) {
        boolean s1 = counts[1] == 1 && counts[2] == 1 && counts[3] == 1 && counts[4] == 1 && counts[5] == 1;
        boolean s2 = counts[2] == 1 && counts[3] == 1 && counts[4] == 1 && counts[5] == 1 && counts[6] == 1;
        return s1 || s2;
    }

    // Getters for GUI
    public int[] getDice() { return dice; }
    public boolean[] getKeep() { return keep; }
    public int getRerolls() { return rerollsLeft; }
    public int getSessionScore() { return totalScore; }
    public String getStatus() { return lastStatus; }
}
