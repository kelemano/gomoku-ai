package com.gomoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

/**
 * Implements the Minimax algorithm with Alpha-Beta Pruning to find the
 * optimal move for the AI player.
 *
 * This implementation includes two key optimizations:
 * 1.  **Relevant Move Generation**: Instead of searching the entire 15x15 board,
 * the algorithm only considers empty cells within a small radius
 * of existing pieces. This drastically reduces the "branching factor".
 * 2.  **Move Ordering**: Before performing a deep search, the algorithm
 * performs a shallow search (depth 0) to get a quick heuristic score
 * for each possible move. It then sorts these moves, exploring the
 * most promising ones first. This makes Alpha-Beta Pruning
 * significantly more effective.
 */
public class MinimaxAI {
    private final int aiPlayer;
    private final int opponent;
    private final int maxDepth;
    private final GameLogic logic;
    private final int boardSize;
    private final Evaluator evaluator;

    // A score far greater than any heuristic evaluation, used to represent a forced win.
    private static final int WIN_SCORE = 1000000;
    // The radius (in cells) around existing pieces to search for valid moves.
    private static final int MOVE_GENERATION_RADIUS = 2;

    /**
     * A simple inner class to represent a move (a coordinate).
     * Used by HashSet to store and check for duplicate moves efficiently.
     */
    private static class Move {
        final int r, c;
        Move(int r, int c) { this.r = r; this.c = c; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return r == move.r && c == move.c;
        }

        @Override
        public int hashCode() {
            return 31 * r + c; // A simple hash for coordinates
        }
    }

    /**
     * An inner class that pairs a Move with its heuristic score.
     * This is used to sort moves before passing them to the recursive minimax function.
     */
    private static class MoveScore {
        final Move move;
        final int score;
        MoveScore(Move move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    /**
     * Constructs the MinimaxAI.
     * @param aiPlayer The integer ID for the AI player (e.g., Board.PLAYER_O).
     * @param opponent The integer ID for the human player (e.g., Board.PLAYER_X).
     * @param maxDepth The maximum ply (half-moves) to search.
     * @param logic The GameLogic object used for win-checking.
     * @param boardSize The size of the board (e.g., 15).
     */
    public MinimaxAI(int aiPlayer, int opponent, int maxDepth, GameLogic logic, int boardSize) {
        this.aiPlayer = aiPlayer;
        this.opponent = opponent;
        this.maxDepth = maxDepth;
        this.logic = logic;
        this.boardSize = boardSize;
        this.evaluator = new Evaluator(logic, 5, boardSize, aiPlayer, opponent);
    }

    /**
     * The main public method to find the best move on the current board.
     * @param board The current board state.
     * @return An integer array [row, col] representing the best move.
     */
    public int[] findBestMove(Board board) {
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        // 1. Generate only relevant moves (near existing pieces)
        List<Move> possibleMoves = getRelevantMoves(board);

        // 2. Score and sort these moves for optimal pruning
        List<MoveScore> scoredMoves = new ArrayList<>();
        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, aiPlayer);

            // Check for an immediate, game-winning move
            if (logic.checkWin(move.r, move.c, aiPlayer)) {
                board.setCell(move.r, move.c, Board.EMPTY); // Undo
                return new int[]{move.r, move.c};
            }

            // Get the shallow heuristic score for this move
            scoredMoves.add(new MoveScore(move, evaluator.evaluate(board)));
            board.setCell(move.r, move.c, Board.EMPTY); // Undo
        }

        // Sort moves from best (highest score) to worst
        scoredMoves.sort(Comparator.comparingInt((MoveScore ms) -> ms.score).reversed());

        // 3. Perform the deep search (minimax) on the sorted moves
        for (MoveScore moveScore : scoredMoves) {
            Move move = moveScore.move;
            int r = move.r;
            int c = move.c;

            // Make the move
            board.setCell(r, c, aiPlayer);
            // Call the recursive helper
            int score = minimax(board, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, r, c);
            // Undo the move
            board.setCell(r, c, Board.EMPTY);

            // Update the best move found so far
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        // Fallback in case no move is found (e.g., draw)
        if (bestMove == null) {
            if (!possibleMoves.isEmpty()) {
                return new int[]{possibleMoves.get(0).r, possibleMoves.get(0).c};
            } else {
                return findFirstEmptyCell(board);
            }
        }

        return new int[]{bestMove.r, bestMove.c};
    }

    /**
     * Generates a list of empty cells that are "relevant" to the game,
     * defined as being within MOVE_GENERATION_RADIUS of any existing piece.
     * This prevents the AI from searching moves in empty, remote corners.
     * @param board The current board state.
     * @return A List of valid, relevant moves.
     */
    private List<Move> getRelevantMoves(Board board) {
        // Use a Set to automatically handle duplicate moves
        Set<Move> moves = new HashSet<>();
        boolean hasAnyPiece = false;

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                // If this cell is occupied, look at its neighbors
                if (board.getCell(r, c) != Board.EMPTY) {
                    hasAnyPiece = true;
                    // Iterate in a square radius around the piece
                    for (int dr = -MOVE_GENERATION_RADIUS; dr <= MOVE_GENERATION_RADIUS; dr++) {
                        for (int dc = -MOVE_GENERATION_RADIUS; dc <= MOVE_GENERATION_RADIUS; dc++) {
                            if (dr == 0 && dc == 0) continue; // Skip self

                            int nr = r + dr;
                            int nc = c + dc;

                            // If the neighbor is valid and empty, add it to the set
                            if (board.isValid(nr, nc) && board.getCell(nr, nc) == Board.EMPTY) {
                                moves.add(new Move(nr, nc));
                            }
                        }
                    }
                }
            }
        }

        // If the board is completely empty, just play in the center.
        if (!hasAnyPiece) {
            moves.add(new Move(boardSize / 2, boardSize / 2));
        }

        return new ArrayList<>(moves);
    }

    /**
     * A fallback method to find the first available empty cell.
     * Used only in rare edge cases (like a full board).
     */
    private int[] findFirstEmptyCell(Board board) {
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    return new int[]{r, c};
                }
            }
        }
        return new int[]{-1, -1}; // No empty cells
    }


    /**
     * The private recursive helper for the Minimax algorithm.
     *
     * @param board The current board state.
     * @param depth The remaining depth to search.
     * @param alpha The best score found so far for the Maximizing player.
     * @param beta The best score found so far for the Minimizing player.
     * @param isMaximizingPlayer True if this node is for the AI, false for the Opponent.
     * @param lastR The row of the move that *led* to this state.
     * @param lastC The col of the move that *led* to this state.
     * @return The heuristic score for this board state.
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer, int lastR, int lastC) {

        // 1. Terminal State Check (Win/Loss)
        // Check if the *previous* move (by the other player) resulted in a win.
        if (isMaximizingPlayer) {
            // The minimizer (opponent) just moved at (lastR, lastC)
            if (logic.checkWin(lastR, lastC, opponent)) {
                // Return a score penalized by depth (prefers losing later)
                return -WIN_SCORE * (depth + 1);
            }
        } else {
            // The maximizer (AI) just moved at (lastR, lastC)
            if (logic.checkWin(lastR, lastC, aiPlayer)) {
                // Return a score rewarded by depth (prefers winning sooner)
                return WIN_SCORE * (depth + 1);
            }
        }

        // 2. Base Case Check (Depth Limit or Draw)
        List<Move> possibleMoves = getRelevantMoves(board);
        if (depth == 0 || possibleMoves.isEmpty()) {
            // Reached search limit or a draw, return the static evaluation
            return evaluator.evaluate(board);
        }

        // 3. Move Ordering
        // Sort moves at this depth to maximize pruning efficiency.
        List<MoveScore> scoredMoves = new ArrayList<>();
        int playerToMove = isMaximizingPlayer ? aiPlayer : opponent;

        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, playerToMove);
            scoredMoves.add(new MoveScore(move, evaluator.evaluate(board)));
            board.setCell(move.r, move.c, Board.EMPTY); // Undo
        }

        if (isMaximizingPlayer) {
            // Max player wants highest scores first
            scoredMoves.sort(Comparator.comparingInt((MoveScore ms) -> ms.score).reversed());
        } else {
            // Min player wants lowest scores first (their "best" move)
            scoredMoves.sort(Comparator.comparingInt((MoveScore ms) -> ms.score));
        }

        // 4. Recursive Search
        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (MoveScore moveScore : scoredMoves) {
                Move move = moveScore.move;

                board.setCell(move.r, move.c, aiPlayer);
                int eval = minimax(board, depth - 1, alpha, beta, false, move.r, move.c);
                board.setCell(move.r, move.c, Board.EMPTY);

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Beta Cutoff (Pruning)
                }
            }
            return maxEval;
        }

        // Minimizing Player (Opponent's Turn)
        else {
            int minEval = Integer.MAX_VALUE;
            for (MoveScore moveScore : scoredMoves) {
                Move move = moveScore.move;

                board.setCell(move.r, move.c, opponent);
                int eval = minimax(board, depth - 1, alpha, beta, true, move.r, move.c);
                board.setCell(move.r, move.c, Board.EMPTY);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Alpha Cutoff (Pruning)
                }
            }
            return minEval;
        }
    }
}