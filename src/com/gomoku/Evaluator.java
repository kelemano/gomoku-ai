package com.gomoku;

/**
 * Provides the heuristic evaluation function for the Minimax algorithm.
 * It assigns a score to the current board state based on potential winning lines.
 */
public class Evaluator {
    // Assign high values to important streaks
    private static final int WINNING_SCORE = 100000; // Value for an immediate win
    private static final int FOUR_OPEN = 10000;    // Four in a row, open on both ends (critical)
    private static final int THREE_OPEN = 1000;    // Three in a row, open on both ends
    private static final int TWO_OPEN = 100;       // Two in a row, open on both ends

    private final GameLogic logic;
    private final int winStreak;
    private final int boardSize;

    /**
     * Constructor
     */
    public Evaluator(GameLogic logic, int winStreak, int boardSize) {
        this.logic = logic;
        this.winStreak = winStreak;
        this.boardSize = boardSize;
    }

    /**
     * The main evaluation function.
     * @param board The current state of the board.
     * @param currentPlayer The player who just made the move (or whose score we are maximizing).
     * @return The integer score of the current board state.
     */
    public int evaluate(Board board, int currentPlayer) {
        int score = 0;

        // TODO: First, check for immediate win using GameLogic.

        // Iterate through all possible cells to find scoring streaks
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) != Board.EMPTY) {
                    // Check horizontal, vertical and both diagonals starting from (r, c)
                    score += evaluateCell(board, r, c);
                }
            }
        }

        // TODO: Adjust score based on which player we are maximizing (currentPlayer vs Opponent)
        return score;
    }

    /**
     * Helper method to analyze streaks originating from a single cell (r, c).
     */
    private int evaluateCell(Board board, int r, int c) {
        // TODO: Implement the detailed logic for checking 4 directions (H, V, D1, D2)
        return 0; // Placeholder
    }
}
