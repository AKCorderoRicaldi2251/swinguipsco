import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SystemCrashDice {
    private Random randGen = new Random();

    private int userNumDiceRemaining = 5;
    private int compNumDiceRemaining = 5;

    private int activeDiceValue = 0;
    private int currentBidQuantity = 0;
    private Player currentPlayer;
    private SceneManager sceneManager;
    private ArrayList<Integer> userDiceValues = new ArrayList<>();
    private ArrayList<Integer> compDiceValues = new ArrayList<>();

    private String lastResultMessage = "Start the Bidding!";
    private boolean roundOver = false;

    public SystemCrashDice(SceneManager manager) {
        this.sceneManager = manager;
        this.currentPlayer = manager.getPlayer();
        startNewRound();
    }

    public void startNewRound() {
        userDiceValues.clear();
        compDiceValues.clear();
        activeDiceValue = 0;
        currentBidQuantity = 0;
        roundOver = false;

        // Roll Player Dice
        for(int i=0; i < userNumDiceRemaining; i++) userDiceValues.add(randGen.nextInt(6) + 1);
        // Roll Computer Dice
        for(int i=0; i < compNumDiceRemaining; i++) compDiceValues.add(randGen.nextInt(6) + 1);

        lastResultMessage = "New Round Started. Your turn!";
    }

    public void playerBid(int face, int qty) {
        if (qty > currentBidQuantity && face >= 2 && face <= 6) {
            activeDiceValue = face;
            currentBidQuantity = qty;
            lastResultMessage = "You bid " + qty + " " + face + "s.";
        } else {
            lastResultMessage = "Invalid Bid! Must raise quantity.";
        }
    }

    public void playerCallLiar() {
        resolveChallenge(false); // False means player is the challenger
    }

    /**
     * Preserved "Davy Jones" Logic from your original snippet
     */
    public boolean computerTurn() {
        if (roundOver) return true;


        int chaosRoll = randGen.nextInt(100);

        // 10% Chance: Greedy
        if (chaosRoll < 10) {
            currentBidQuantity++;
            if (randGen.nextBoolean()) {
                activeDiceValue = randGen.nextInt(5) + 2;
            }
            lastResultMessage = "DAVY: (Greedy) " + currentBidQuantity + " " + activeDiceValue + "s!";
            return false;
        }

        // 10% Chance: Paranoid
        if (chaosRoll > 90) {
            lastResultMessage = "DAVY: I don't trust you! LIAR!";
            resolveChallenge(true); // AI is challenger
            return true;
        }

        // --- STEP 2: THE MATH ---
        int myMatches = 0;
        int myBugs = 0;
        int myPatches = 0;

        for (int die : compDiceValues) {
            if (die == activeDiceValue) myMatches++;
            if (die == 1) myBugs++;
            if (die == 6) myPatches++;
        }

        int effectiveBugs = Math.max(0, myBugs - myPatches);
        int myStrength = myMatches + effectiveBugs;

        // Fuzzy Math estimation
        double estimatedPlayerProb = 0.10 + (randGen.nextDouble() * 0.15);
        double expectedPlayerStrength = userNumDiceRemaining * estimatedPlayerProb;
        double totalEstimate = myStrength + expectedPlayerStrength;

        // --- STEP 3: THE DECISION ---
        if (currentBidQuantity > totalEstimate) {
            lastResultMessage = "DAVY: Logic says you're lying! LIAR!";
            resolveChallenge(true);
            return true;
        } else {

            if (myMatches == 0 && myBugs > 0) {
                activeDiceValue = randGen.nextInt(5) + 2;
            }
            currentBidQuantity++;
            lastResultMessage = "DAVY: I'll raise to " + currentBidQuantity + " " + activeDiceValue + "s.";
            return false;
        }
    }

    private void resolveChallenge(boolean computerIsChallenger) {
        int totalMatches = 0, totalBugs = 0, totalPatches = 0;

        // Count both hands
        List<Integer> allDice = new ArrayList<>(userDiceValues);
        allDice.addAll(compDiceValues);

        for (int d : allDice) {
            if (d == activeDiceValue) totalMatches++;
            if (d == 1) totalBugs++;
            if (d == 6) totalPatches++;
        }

        int remainingBugs = Math.max(0, totalBugs - totalPatches);
        int finalScore = totalMatches + remainingBugs;
        boolean bidderLied = (finalScore < currentBidQuantity);

        String winnerStr;
        if (computerIsChallenger) {
            if (bidderLied) {
                winnerStr = "You Lied! Jones wins.";
                userNumDiceRemaining--;
            } else {
                winnerStr = "Honest! Jones loses a die.";
                compNumDiceRemaining--;
            }
        } else {
            if (bidderLied) {
                winnerStr = "Jones Lied! You win.";
                compNumDiceRemaining--;
            } else {
                winnerStr = "Jones was honest! You lose.";
                userNumDiceRemaining--;
            }
        }

        lastResultMessage = String.format("Audit: %d found. %s", finalScore, winnerStr);
        roundOver = true;
        checkGameOver();
    }
    private void checkGameOver() {
        // Check if anyone has run out of dice
        if (userNumDiceRemaining <= 0 || compNumDiceRemaining <= 0) {

            if (compNumDiceRemaining <= 0) {
                // Computer has no dice left = User Wins
                lastResultMessage = "SYSTEM RECOVERED: YOU WIN!";
                currentPlayer.incrementScore("SystemCrash_Wins");
            } else if (userNumDiceRemaining <= 0) {
                // User has no dice left = User Loses
                lastResultMessage = "CRITICAL FAILURE: YOU LOSE.";
                currentPlayer.incrementScore("SystemCrash_Losses");
            }

            // Save the progress immediately
            GameDataManager.savePlayer(currentPlayer);
        }
    }

    // --- GUI GETTERS ---
    public List<Integer> getPlayerDice() { return userDiceValues; }
    public int getPlayerDiceCount() { return userNumDiceRemaining; }
    public int getDealerDiceCount() { return compNumDiceRemaining; }
    public int getActiveDiceValue() { return activeDiceValue; }
    public int getCurrentBidQuantity() { return currentBidQuantity; }
    public String getLastResultMessage() { return lastResultMessage; }
    public boolean isRoundOver() { return roundOver; }



    public List<Integer> getCompDiceValues() {
        return compDiceValues;
    }
}
