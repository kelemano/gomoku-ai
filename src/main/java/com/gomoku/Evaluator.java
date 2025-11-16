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
    private static final int FIVE = 100000;           // Победа
    private static final int OPEN_FOUR = 50000;       // _XXXX_ (гарантированный выигрыш)
    private static final int FOUR = 10000;            // XXXX_ или _XXXX
    private static final int OPEN_THREE = 5000;       // _XXX_ (двойная угроза)
    private static final int THREE = 1000;            // XXX_ или _XXX
    private static final int OPEN_TWO = 500;          // _XX_
    private static final int TWO = 100;




    private final GameLogic logic;
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

        // Return the net score. A positive score favors the AI.
        return aiScore - opponentScore;
    }


    private int evaluateLine(Board board, int r, int c, int dr, int dc, int player) {
        int totalScore = 0;
        int consecutive = 0;
        int openEnds = 0;
        boolean leftOpen = false;

        int row = r;
        int col = c;

        // Проходим по всей линии
        while (board.isValid(row, col)) {
            int cell = board.getCell(row, col);

            if (cell == player) {
                // Продолжаем последовательность
                consecutive++;
            } else {
                // Последовательность прервана
                if (consecutive > 0) {
                    // Проверяем правый конец
                    boolean rightOpen = (cell == Board.EMPTY);

                    // Оцениваем найденную последовательность
                    totalScore += scorePattern(consecutive, leftOpen, rightOpen);

                    consecutive = 0;
                }

                // Запоминаем, открыт ли левый конец следующей последовательности
                leftOpen = (cell == Board.EMPTY);
            }

            row += dr;
            col += dc;
        }

        // Оцениваем последнюю последовательность
        if (consecutive > 0) {
            totalScore += scorePattern(consecutive, leftOpen, false);
        }

        return totalScore;
    }

    /**
     * Оценивает паттерн по длине и открытым концам
     */
    private int scorePattern(int length, boolean leftOpen, boolean rightOpen) {
        if (length >= 5) {
            return FIVE;
        }

        boolean open = leftOpen && rightOpen;
        boolean halfOpen = leftOpen || rightOpen;

        return switch (length) {
            case 4 -> open ? OPEN_FOUR : (halfOpen ? FOUR : 0);
            case 3 -> open ? OPEN_THREE : (halfOpen ? THREE : 0);
            case 2 -> open ? OPEN_TWO : (halfOpen ? TWO : 0);
            default -> 0;
        };
    }

}