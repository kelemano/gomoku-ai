package com.gomoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class responsible for checking the game state,
 * particularly the win condition (five-in-a-row).
 */
public class GameLogic {
    private final Board board;
    private final int winStreak;

    /**
     * Constructor.
     * @param board The current game board instance.
     * @param winStreak The required streak length for a win (5).
     */
    public GameLogic(Board board, int winStreak) {
        this.board = board;
        this.winStreak = winStreak;
    }

    /**
     * Checks if the last move resulted in a win for the specified player.
     * It checks all four major directions from the cell of the last move.
     *
     * @param lastR The row index of the last move.
     * @param lastC The column index of the last move.
     * @param player The ID of the player who made the move.
     * @return true if the player has achieved the winStreak, false otherwise.
     */
    public boolean checkWin(int lastR, int lastC, int player) {
        if (player == Board.EMPTY) return false;

        // Check the four main directions:
        // (dr, dc): (row_change, col_change)
        return checkDirection(lastR, lastC, player, 0, 1) ||  // 1. Horizontal (right/left)
                checkDirection(lastR, lastC, player, 1, 0) ||  // 2. Vertical (down/up)
                checkDirection(lastR, lastC, player, 1, 1) ||  // 3. Main Diagonal (\)
                checkDirection(lastR, lastC, player, 1, -1);   // 4. Anti-Diagonal (/)
    }

    /**
     * Helper method to count the longest streak in both directions along the line (dr, dc).
     *
     * @param r Start row.
     * @param c Start column.
     * @param player Player ID to check.
     * @param dr Row direction change.
     * @param dc Column direction change.
     * @return True if a streak >= winStreak is found.
     */
    private boolean checkDirection(int r, int c, int player, int dr, int dc) {
        // Start count at 1 because the placed piece (r, c) is always part of the streak.
        int streak = 1;

        // Count in the positive direction (right, down)
        for (int i = 1; i < winStreak; i++) {
            if (board.isValid(r + dr * i, c + dc * i) && board.getCell(r + dr * i, c + dc * i) == player) {
                streak++;
            } else {
                break;
            }
        }

        // Check if a win was already found in the first part
        if (streak >= winStreak) {
            return true;
        }

        // Count in the opposite direction (left, up)
        for (int i = 1; i < winStreak; i++) {
            if (board.isValid(r - dr * i, c - dc * i) && board.getCell(r - dr * i, c - dc * i) == player) {
                streak++;
            } else {
                break;
            }
        }

        // Final check for the combined streak
        return streak >= winStreak;
    }

    /**
     * НОВЫЙ МЕТОД: Находит и возвращает 5 координат победной линии.
     * @return Список из 5+ координат [r, c] или null, если победы нет.
     */
    public List<int[]> findWinningLine(int lastR, int lastC, int player) {
        if (player == Board.EMPTY) return null;

        List<int[]> line;

        line = getLineCoordinates(lastR, lastC, player, 0, 1); // Horizontal
        if (line != null) return line;

        line = getLineCoordinates(lastR, lastC, player, 1, 0); // Vertical
        if (line != null) return line;

        line = getLineCoordinates(lastR, lastC, player, 1, 1); // Main Diagonal
        if (line != null) return line;

        line = getLineCoordinates(lastR, lastC, player, 1, -1); // Anti-Diagonal
        if (line != null) return line;

        return null; // Победы нет
    }

    /**
     * НОВЫЙ МЕТОД: Собирает координаты фишек в одной линии.
     * @return Список координат, если их >= winStreak, иначе null.
     */
    private List<int[]> getLineCoordinates(int r, int c, int player, int dr, int dc) {
        List<int[]> line = new ArrayList<>();
        line.add(new int[]{r, c}); // Добавляем фишку, которой сходили

        // Идем в положительном направлении
        for (int i = 1; i < winStreak; i++) {
            int nr = r + dr * i;
            int nc = c + dc * i;
            if (board.isValid(nr, nc) && board.getCell(nr, nc) == player) {
                line.add(new int[]{nr, nc});
            } else {
                break;
            }
        }

        // Идем в отрицательном направлении
        for (int i = 1; i < winStreak; i++) {
            int nr = r - dr * i;
            int nc = c - dc * i;
            if (board.isValid(nr, nc) && board.getCell(nr, nc) == player) {
                line.add(new int[]{nr, nc});
            } else {
                break;
            }
        }

        // Если фишек 5 или больше, возвращаем линию
        return (line.size() >= winStreak) ? line : null;
    }
}
