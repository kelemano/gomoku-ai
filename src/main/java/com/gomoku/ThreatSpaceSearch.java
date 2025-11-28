package com.gomoku;

import java.util.ArrayList;
import java.util.List;

/**
 * Specialized search component for identifying critical game states (Threats).
 * <p>
 * This class drastically reduces the branching factor for the Minimax algorithm by
 * filtering for "forcing" moves. Instead of evaluating all ~50 available moves,
 * it identifies the ~5-10 moves that are strictly necessary to consider (e.g.,
 * winning immediately, blocking an opponent's win, or creating strong attacks).
 */
public class ThreatSpaceSearch {
    private final Board board;
    private final GameLogic logic;
    private final int boardSize;

    /**
     * Constructs the ThreatSpaceSearch.
     *
     * @param board     The current game board.
     * @param logic     The game logic for win checks.
     * @param boardSize The size of the board.
     */
    public ThreatSpaceSearch(Board board, GameLogic logic, int boardSize) {
        this.board = board;
        this.logic = logic;
        this.boardSize = boardSize;
    }

    /**
     * Identifies critical moves for the given player.
     * <p>
     * Priorities:
     * 1. Win immediately.
     * 2. Block opponent's immediate win.
     * 3. Create strong threats (Open Fours, Open Threes).
     * 4. Block opponent's threats.
     *
     * @param player   The ID of the current player.
     * @param opponent The ID of the opponent.
     * @return A list of critical moves to consider.
     */
    public List<Move> findThreats(int player, int opponent) {
        List<Move> threats = new ArrayList<>();

        // 1. CRITICAL: Check for immediate win
        List<Move> winningMoves = findWinningMoves(player);
        if (!winningMoves.isEmpty()) {
            return winningMoves;
        }

        // 2. CRITICAL: Block opponent's immediate win
        List<Move> blockingMoves = findWinningMoves(opponent);
        if (!blockingMoves.isEmpty()) {
            threats.addAll(blockingMoves);
            return threats;
        }

        // 3. Find forcing moves (creating Open Fours/Threes)
        threats.addAll(findOpenFours(player));
        threats.addAll(findOpenThrees(player));

        // 4. Block opponent's threats
        threats.addAll(findOpenFours(opponent));
        threats.addAll(findOpenThrees(opponent));

        return threats;
    }

    /**
     * Finds moves that result in an immediate win (5 in a row).
     */
    private List<Move> findWinningMoves(int player) {
        List<Move> moves = new ArrayList<>();

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    // Simulate move
                    board.setCell(r, c, player);

                    if (logic.checkWin(r, c, player)) {
                        moves.add(new Move(r, c));
                    }

                    // Undo move
                    board.setCell(r, c, Board.EMPTY);
                }
            }
        }
        return moves;
    }

    /**
     * Finds moves that create an Open Four pattern (_XXXX_).
     * This is a very strong threat (guaranteed win next turn unless blocked).
     */
    private List<Move> findOpenFours(int player) {
        List<Move> moves = new ArrayList<>();

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    board.setCell(r, c, player);

                    if (hasOpenFour(r, c, player)) {
                        moves.add(new Move(r, c));
                    }

                    board.setCell(r, c, Board.EMPTY);
                }
            }
        }
        return moves;
    }

    /**
     * Finds moves that create an Open Three pattern (_XXX_).
     */
    private List<Move> findOpenThrees(int player) {
        List<Move> moves = new ArrayList<>();

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    board.setCell(r, c, player);

                    if (hasOpenThree(r, c, player)) {
                        moves.add(new Move(r, c));
                    }
                    board.setCell(r, c, Board.EMPTY);
                }
            }
        }
        return moves;
    }

    /**
     * Checks if placing a piece at (r, c) creates an Open Four.
     */
    private boolean hasOpenFour(int r, int c, int player) {
        // Directions: Horizontal, Vertical, Diagonal, Anti-Diagonal
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1; // Start with the placed piece
            int leftOpen = 0;
            int rightOpen = 0;

            // Scan positive direction
            for (int i = 1; i <= 4; i++) {
                int nr = r + dir[0] * i;
                int nc = c + dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    rightOpen = 1;
                    break;
                } else {
                    break; // Blocked by opponent
                }
            }

            // Scan negative direction
            for (int i = 1; i <= 4; i++) {
                int nr = r - dir[0] * i;
                int nc = c - dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    leftOpen = 1;
                    break;
                } else {
                    break;
                }
            }

            // Open Four: 4 pieces total, open on both ends
            if (count == 4 && leftOpen == 1 && rightOpen == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if placing a piece at (r, c) creates an Open Three.
     */
    private boolean hasOpenThree(int r, int c, int player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1;
            int leftOpen = 0;
            int rightOpen = 0;

            // Scan positive direction
            for (int i = 1; i <= 3; i++) {
                int nr = r + dir[0] * i;
                int nc = c + dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    rightOpen = 1;
                    break;
                } else {
                    break;
                }
            }

            // Scan negative direction
            for (int i = 1; i <= 3; i++) {
                int nr = r - dir[0] * i;
                int nc = c - dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    leftOpen = 1;
                    break;
                } else {
                    break;
                }
            }

            // Open Three: 3 pieces total, open on both ends
            if (count == 3 && leftOpen == 1 && rightOpen == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper class representing a move coordinates.
     */
    public static class Move {
        public final int r, c;

        public Move(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return r == move.r && c == move.c;
        }

        @Override
        public int hashCode() {
            return 31 * r + c;
        }
    }
}
