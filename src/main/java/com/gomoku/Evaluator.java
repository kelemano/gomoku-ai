package com.gomoku;

/**
 * Provides the heuristic evaluation function for the Minimax algorithm.
 *
 * This evaluator uses a "sliding window" approach. It iterates across the board,
 * looking at all possible lines (horizontal, vertical, and diagonals) and assigns
 * a score to each pattern found (e.g., Open Four, Live Three).
 *
 * The final score is the difference between the AI's score and the Opponent's score.
 * This approach prevents double-counting and remains efficient (O(N^2)).
 */
public class Evaluator {

    // === Heuristic Scores ===
    // These values determine the AI's priorities.
    // They are separated by orders of magnitude to strictly prioritize stronger threats.
    private static final int FIVE = 100000; // Win
    private static final int OPEN_FOUR = 50000; // _XXXX_ (Guaranteed win next turn)
    private static final int FOUR = 10000; // XXXX_ or _XXXX (Forced block)
    private static final int OPEN_THREE = 5000; // _XXX_ (Major threat)
    private static final int THREE = 1000; // XXX_ or _XXX
    private static final int OPEN_TWO = 500; // _XX_
    private static final int TWO = 100; // XX_ or _XX

    private final int boardSize;
    private final int aiPlayer;
    private final int opponent;


    /**
     * Constructs the Evaluator.
     *
     * @param boardSize The size of the game board.
     * @param aiPlayer  The ID of the AI player.
     * @param opponent  The ID of the opponent (human).
     */
    public Evaluator(int boardSize, int aiPlayer, int opponent) {
        this.boardSize = boardSize;
        this.aiPlayer = aiPlayer;
        this.opponent = opponent;
    }

    /**
     * Calculates the heuristic score of the current board state.
     *
     * @param board The current state of the board.
     * @return The net score (AI Score - Opponent Score). Positive values favor the AI.
     */
    public int evaluate(Board board) {
        int aiScore = 0;
        int opponentScore = 0;

        // 1. Evaluate Horizontals
        for (int r = 0; r < boardSize; r++) {
            aiScore += evaluateLine(board, r, 0, 0, 1, aiPlayer); // AI's score
            opponentScore += evaluateLine(board, r, 0, 0, 1, opponent); // Opponent's score

        }

        // 2. Evaluate Verticals
        for (int c = 0; c < boardSize; c++) {
            aiScore += evaluateLine(board, 0, c, 1, 0, aiPlayer);
            opponentScore += evaluateLine(board, 0, c, 1, 0, opponent);
        }

        // 3. Evaluate Main Diagonals (\)
        for (int r = 0; r < boardSize; r++) {
                aiScore += evaluateLine(board, r,0, 1, 1, aiPlayer);
                opponentScore += evaluateLine(board, r, 0, 1, 1, opponent);
        }
        for (int c = 1; c < boardSize; c++) {
            aiScore += evaluateLine(board, 0,c, 1, 1, aiPlayer);
            opponentScore += evaluateLine(board, 0,c, 1, 1, opponent);
        }

        // 4. Evaluate Anti-Diagonals (/)
        for (int r = 0; r < boardSize; r++) { // Start from row `winStreak-1` (e.g., row 4)
            aiScore += evaluateLine(board, r, boardSize - 1, 1, -1, aiPlayer);
            opponentScore += evaluateLine(board, r, boardSize - 1, 1, -1, opponent);
        }
        for (int c = boardSize - 2; c >= 0; c--) {
            aiScore += evaluateLine(board, 0, c, 1, -1, aiPlayer);
            opponentScore += evaluateLine(board, 0, c, 1, -1, opponent);
        }

        // Return the net score
        return aiScore - opponentScore;
    }

    /**
     * Scans a specific line (row, column, or diagonal) for patterns created by the given player.
     */
    private int evaluateLine(Board board, int r, int c, int dr, int dc, int player) {
        int totalScore = 0;
        int consecutive = 0;
        boolean leftOpen = false;

        int row = r;
        int col = c;

        // Iterate through the line
        while (board.isValid(row, col)) {
            int cell = board.getCell(row, col);

            if (cell == player) {
                // Continue the streak
                consecutive++;
            } else {
                // Streak broken
                if (consecutive > 0) {
                    // Check if the right end is open (empty cell)
                    boolean rightOpen = (cell == Board.EMPTY);

                    // Score the completed pattern
                    totalScore += scorePattern(consecutive, leftOpen, rightOpen);
                    consecutive = 0;
                }
                // If the current cell is empty, it becomes the "left open" for the NEXT streak
                leftOpen = (cell == Board.EMPTY);
            }
            row += dr;
            col += dc;
        }
        // If the current cell is empty, it becomes the "left open" for the NEXT streak
        if (consecutive > 0) {
            // The right end is the board edge (closed)
            totalScore += scorePattern(consecutive, leftOpen, false);
        }
        return totalScore;
    }

    /**
     * Assigns a score based on the length of the streak and whether its ends are open.
     */
    private int scorePattern(int length, boolean leftOpen, boolean rightOpen) {
        if (length >= 5) {
            return FIVE;
        }

        boolean open = leftOpen && rightOpen; // Both ends open (e.g., _XXX_)
        boolean halfOpen = leftOpen || rightOpen; // One end open (e.g., XXX_)

        return switch (length) {
            case 4 -> open ? OPEN_FOUR : (halfOpen ? FOUR : 0);
            case 3 -> open ? OPEN_THREE : (halfOpen ? THREE : 0);
            case 2 -> open ? OPEN_TWO : (halfOpen ? TWO : 0);
            default -> 0;
        };
    }
}