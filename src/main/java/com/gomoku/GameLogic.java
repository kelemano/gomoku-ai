package com.gomoku;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the core rules of Gomoku.
 * <p>
 * This class is responsible for determining game outcomes, specifically checking
 * if a move results in a win (Five-in-a-row) and identifying the winning line coordinates.
 */
public class GameLogic {
    private final Board board;
    private final int winStreak;

    /**
     * Constructs the GameLogic.
     *
     * @param board     The game board to analyze.
     * @param winStreak The number of consecutive pieces required to win (usually 5).
     */
    public GameLogic(Board board, int winStreak) {
        this.board = board;
        this.winStreak = winStreak;
    }

    /**
     * Checks if the last move made by a player resulted in a win.
     * Scans all four axes (Horizontal, Vertical, Diagonal, Anti-Diagonal) from the placement point.
     *
     * @param lastR  The row index of the last move.
     * @param lastC  The column index of the last move.
     * @param player The ID of the player who made the move.
     * @return true if the move created a winning streak, false otherwise.
     */
    public boolean checkWin(int lastR, int lastC, int player) {
        if (player == Board.EMPTY) return false;

        // Check the four main directions (dr, dc):
        return checkDirection(lastR, lastC, player, 0, 1) ||  // 1. Horizontal (right/left)
                checkDirection(lastR, lastC, player, 1, 0) ||  // 2. Vertical (down/up)
                checkDirection(lastR, lastC, player, 1, 1) ||  // 3. Main Diagonal (\)
                checkDirection(lastR, lastC, player, 1, -1);   // 4. Anti-Diagonal (/)
    }

    /**
     * Helper method to count the longest streak of the player's pieces passing through (r, c)
     * in a specific direction defined by (dr, dc).
     */
    private boolean checkDirection(int r, int c, int player, int dr, int dc) {
        int streak = 1; // Start with the piece itself

        // 1. Scan in the positive direction
        for (int i = 1; i < winStreak; i++) {
            if (board.isValid(r + dr * i, c + dc * i) &&
                    board.getCell(r + dr * i, c + dc * i) == player) {
                streak++;
            } else {
                break;
            }
        }

        // Optimization: Early exit if we already won
        if (streak >= winStreak) return true;

        // 2. Scan in the negative direction
        for (int i = 1; i < winStreak; i++) {
            if (board.isValid(r - dr * i, c - dc * i) &&
                    board.getCell(r - dr * i, c - dc * i) == player) {
                streak++;
            } else {
                break;
            }
        }
        return streak >= winStreak;
    }

    /**
     * Identifies and returns the coordinates of the winning line.
     * This is used by the GUI to highlight the winning pieces.
     *
     * @param lastR  The row index of the winning move.
     * @param lastC  The column index of the winning move.
     * @param player The winning player ID.
     * @return A list of coordinates {row, col} representing the winning streak, or null if no win found.
     */
    public List<int[]> findWinningLine(int lastR, int lastC, int player) {
        if (player == Board.EMPTY) return null;

        List<int[]> line;

        // Check all directions and return the first valid winning line found
        line = getLineCoordinates(lastR, lastC, player, 0, 1); // Horizontal
        if (line != null) return line;

        line = getLineCoordinates(lastR, lastC, player, 1, 0); // Vertical
        if (line != null) return line;

        line = getLineCoordinates(lastR, lastC, player, 1, 1); // Main Diagonal
        if (line != null) return line;

        line = getLineCoordinates(lastR, lastC, player, 1, -1); // Anti-Diagonal
        if (line != null) return line;

        return null;
    }

    /**
     * Collects the coordinates of a consecutive streak in a given direction.
     *
     * @return A list of coordinates if the streak length >= winStreak, otherwise null.
     */
    private List<int[]> getLineCoordinates(int r, int c, int player, int dr, int dc) {
        List<int[]> line = new ArrayList<>();
        line.add(new int[]{r, c}); // Add the starting piece

        // Scan positive direction
        for (int i = 1; i < winStreak; i++) {
            int nr = r + dr * i;
            int nc = c + dc * i;
            if (board.isValid(nr, nc) && board.getCell(nr, nc) == player) {
                line.add(new int[]{nr, nc});
            } else {
                break;
            }
        }

        // Scan negative direction
        for (int i = 1; i < winStreak; i++) {
            int nr = r - dr * i;
            int nc = c - dc * i;
            if (board.isValid(nr, nc) && board.getCell(nr, nc) == player) {
                line.add(new int[]{nr, nc});
            } else {
                break;
            }
        }
        // Return the line only if it constitutes a win
        return (line.size() >= winStreak) ? line : null;
    }
}
