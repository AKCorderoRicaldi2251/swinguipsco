import java.util.Random;

public class DiceGridEngine {
    private int[][] grid = new int[3][3];
    private int currentRoll;
    private Random rand = new Random();
    private String status = "Roll to place!";
    private Player player;

    public DiceGridEngine(Player player) {
        this.player = player;
        rollNewDie();
    }

    public void rollNewDie() {
        currentRoll = rand.nextInt(6) + 1;
        status = "Current Data: " + currentRoll;
    }

    public boolean placeDie(int r, int c) {
        if (grid[r][c] == 0) {
            grid[r][c] = currentRoll;
            if (isFull()) {
                status = "GRID_COMPLETE!";
            } else {
                rollNewDie();
            }
            return true;
        }
        status = "SLOT_OCCUPIED!";
        return false;
    }

    public boolean isFull() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (grid[r][c] == 0) return false;
            }
        }
        return true;
    }

    public int calculateScore() {
        int total = 0;
        // Rows
        for (int r = 0; r < 3; r++) total += scoreLine(grid[r][0], grid[r][1], grid[r][2]);
        // Columns
        for (int c = 0; c < 3; c++) total += scoreLine(grid[0][c], grid[1][c], grid[2][c]);

        // Save to Player Data
        player.setScore("DiceGrid_HighScore", Math.max(player.getScore("DiceGrid_HighScore"), total));
        GameDataManager.savePlayer(player);
        return total;
    }

    private int scoreLine(int a, int b, int c) {
        if (a == b && b == c) return 15; // Three of a Kind
        if (isStraight(a, b, c)) return 12; // Straight
        if (a == b || b == c || a == c) return 8; // Pair
        return 5; // All Different
    }

    private boolean isStraight(int a, int b, int c) {
        int[] arr = {a, b, c};
        java.util.Arrays.sort(arr);
        return arr[1] == arr[0] + 1 && arr[2] == arr[1] + 1;
    }

    public int[][] getGrid() { return grid; }
    public int getCurrentRoll() { return currentRoll; }
    public String getStatus() { return status; }
}