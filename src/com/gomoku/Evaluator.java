package com.gomoku;

/**
 * Provides the heuristic evaluation function for the Minimax algorithm.
 *
 * This evaluator uses a "sliding window" approach. It does not count
 * from "each piece". Instead, it iterates across the board, looking at
 * all possible "lines of 5" (horizontal, vertical, and diagonals)
 * and scores *each line*.
 * This prevents double-counting and is efficient (O(N^2)).
 */
public class Evaluator {
    // Scores for threats.
    // These scores must be orders of magnitude less than WIN_SCORE in MinimaxAI.
    private static final int FIVE_IN_ROW = 100000; // 5 in a row (technically WIN_SCORE)
    private static final int FOUR_IN_ROW = 10000;   // 4 in a row, 0 opponent pieces
    private static final int THREE_IN_ROW = 1000;   // 3 in a row, 0 opponent pieces
    private static final int TWO_IN_ROW = 100;      // 2 in a row, 0 opponent pieces

    // TODO: A more advanced Evaluator would also consider "open ends"
    // (e.g., _XXX_ (open 3) is much more valuable than OXXX_ (closed 3)).
    // But this "simple" evaluation will work for now.

    private final GameLogic logic; // Not used in this version, but kept for potential future use
    private final int winStreak;
    private final int boardSize;
    private final int aiPlayer;
    private final int opponent;

    /**
     * Constructor
     */
    public Evaluator(GameLogic logic, int winStreak, int boardSize, int aiPlayer, int opponent) {
        this.logic = logic;
        this.winStreak = winStreak;
        this.boardSize = boardSize;
        this.aiPlayer = aiPlayer;
        this.opponent = opponent;
    }

    /**
     * The main evaluation function.
     * Iterates through all possible lines (horizontal, vertical, diagonal)
     * and calculates a net score.
     * @param board The current state of the board.
     * @return The NET score (AI_Score - Opponent_Score).
     */
    public int evaluate(Board board) {
        int aiScore = 0;
        int opponentScore = 0;

        // 1. Evaluate Horizontals
        for (int r = 0; r < boardSize; r++) {
            // Slide a window of `winStreak` cells across the row
            for (int c = 0; c <= boardSize - winStreak; c++) {
                aiScore += evaluateWindow(board, r, c, 0, 1, aiPlayer); // AI's score
                opponentScore += evaluateWindow(board, r, c, 0, 1, opponent); // Opponent's score
            }
        }

        // 2. Evaluate Verticals
        for (int c = 0; c < boardSize; c++) {
            // Slide a window down the column
            for (int r = 0; r <= boardSize - winStreak; r++) {
                aiScore += evaluateWindow(board, r, c, 1, 0, aiPlayer);
                opponentScore += evaluateWindow(board, r, c, 1, 0, opponent);
            }
        }

        // 3. Evaluate Main Diagonals (\)
        // (Top-left to bottom-right)
        for (int r = 0; r <= boardSize - winStreak; r++) {
            for (int c = 0; c <= boardSize - winStreak; c++) {
                aiScore += evaluateWindow(board, r, c, 1, 1, aiPlayer);
                opponentScore += evaluateWindow(board, r, c, 1, 1, opponent);
            }
        }

        // 4. Evaluate Anti-Diagonals (/)
        // (Top-right to bottom-left)
        for (int r = winStreak - 1; r < boardSize; r++) { // Start from row `winStreak-1` (e.g., row 4)
            for (int c = 0; c <= boardSize - winStreak; c++) {
                aiScore += evaluateWindow(board, r, c, -1, 1, aiPlayer); // (r-1, c+1)
                opponentScore += evaluateWindow(board, r, c, -1, 1, opponent);
            }
        }

        // Return the net score. A positive score favors the AI.
        return aiScore - opponentScore;
    }

    /**
     * Evaluates a single "window" of `winStreak` (e.g., 5) cells for ONE player.
     *
     * @param board The board state.
     * @param r_start The starting row of the window.
     * @param c_start The starting column of the window.
     * @param dr The row direction delta (0, 1, 1, or -1).
     * @param dc The column direction delta (1, 0, 1, or 1).
     * @param player The player we are scoring this window for.
     * @return The score for this window for this specific player.
     */
    private int evaluateWindow(Board board, int r_start, int c_start, int dr, int dc, int player) {
        int playerCount = 0;
        int opponentCount = 0;

        // Count the pieces for each player within this 5-cell window
        for (int i = 0; i < winStreak; i++) {
            int cell = board.getCell(r_start + i * dr, c_start + i * dc);
            if (cell == player) {
                playerCount++;
            } else if (cell != Board.EMPTY) {
                opponentCount++;
            }
        }

        // If pieces from *both* players are in this window,
        // it's a "dead" or "blocked" window and represents no threat.
        if (playerCount > 0 && opponentCount > 0) {
            return 0;
        }

        // The window contains pieces from only this player (or is empty)
        // Assign score based on the number of pieces.
        return switch (playerCount) {
            case 5 -> FIVE_IN_ROW;
            case 4 -> FOUR_IN_ROW;
            case 3 -> THREE_IN_ROW;
            case 2 -> TWO_IN_ROW;
            default -> 0; // 0 or 1 piece
        };
    }
}