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

    // Direction vectors for checking Horizontal, Vertical, and Diagonals
    private static final int[][] DIRECTIONS = {
            {0, 1},   // Horizontal (r, c+1)
            {1, 0},   // Vertical (r+1, c)
            {1, 1},   // Main Diagonal (r+1, c+1)
            {1, -1}   // Anti Diagonal (r+1, c-1)
    };

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
        int cellScore = 0;
        int player = board.getCell(r, c); // The player who owns this cell

        // Iterate through all 4 directions: H, V, D1, D2
        for (int[] dir : DIRECTIONS) {

            // Analyze the streak in one specific direction (e.g., Horizontal to the right)
            cellScore += evaluateDirection(board, r, c, player, dir[0], dir[1]);
        }

        return cellScore;
    }

    /**
     * Analyzes a single direction (dx, dy) starting from (r, c) for a player's streak.
     * @param board The current board.
     * @param r Start row.
     * @param c Start column.
     * @param player The player whose streak we are checking.
     * @param dr Direction row increment.
     * @param dc Direction column increment.
     * @return The score contribution from this direction.
     */
    private int evaluateDirection(Board board, int r, int c, int player, int dr, int dc) {
        // check for streaks starting at (r, c) and extending in the positive direction (dr, dc).

        int score = 0;
        int streakLength = 0;
        int openEnds = 0; // Number of empty cells available at the ends of the streak

        int r1 = r;
        int c1 = c;

        // Calculate the streak length in the positive direction (dr, dc)
        while (r1 >= 0 && r1 < boardSize && c1 >= 0 && c1 < boardSize && board.getCell(r1, c1) == player) {
            streakLength++;
            r1 += dr;
            c1 += dc;
        }

        // Check the end of the streak (the cell after the streak ends)
        boolean end1Open = false;
        if (r1 >= 0 && r1 < boardSize && c1 >= 0 && c1 < boardSize && board.getCell(r1, c1) == Board.EMPTY) {
            end1Open = true; // The streak is open on the positive end
        }

        // Calculate the streak length in the negative direction (-dr, -dc)
        // We start one step back from the original (r, c) to avoid double counting the original cell.
        int r2 = r - dr;
        int c2 = c - dc;

        while (r2 >= 0 && r2 < boardSize && c2 >= 0 && c2 < boardSize && board.getCell(r2, c2) == player) {
            streakLength++;
            r2 -= dr;
            c2 -= dc;
        }

        // Check the end of the streak on the negative side (the cell before the streak starts)
        boolean end2Open = false;
        if (r2 >= 0 && r2 < boardSize && c2 >= 0 && c2 < boardSize && board.getCell(r2, c2) == Board.EMPTY) {
            end2Open = true; // The streak is open on the negative end
        }

        // Calculate Open Ends
        if (end1Open && end2Open) {
            openEnds = 2; // Open on both sides (e.g., _XXX_)
        } else if (end1Open || end2Open) {
            openEnds = 1; // Open on one side (e.g., XXXXX_) or (_XXXXX)
        }

        // Assign Score Based on Streak Length and Open Ends

        // If the streak is long enough to win:
        if (streakLength >= winStreak) {
            score = WINNING_SCORE;
        }

        // Assign scores for threats (less than 5)
        else if (streakLength == 4) {
            if (openEnds == 2) { // _XXXX_ (Immediate win threat)
                score = FOUR_OPEN;
            } else if (openEnds == 1) { // _XXXXo or oXXXX_ (Must block)
                score = FOUR_OPEN / 10; // Still a high score, but less critical
            }
        } else if (streakLength == 3) {
            if (openEnds == 2) { // _XXX_ (Two threats in two directions)
                score = THREE_OPEN;
            }
        } else if (streakLength == 2) {
            if (openEnds == 2) { // _XX_
                score = TWO_OPEN;
            }
        }

        return score;
    }

}
