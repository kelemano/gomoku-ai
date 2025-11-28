package com.gomoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

/**
 * Implements the Minimax algorithm with Alpha-Beta Pruning to determine the optimal move for the AI.
 * <p>
 * This implementation employs several optimizations to handle the complexity of Gomoku (15x15 board):
 * <ol>
 * <li><b>Threat Space Search:</b> Prioritizes moves that lead to immediate wins or block opponent wins, significantly reducing the branching factor in critical positions.</li>
 * <li><b>Relevant Move Generation:</b> Only considers empty cells within a specific radius of existing pieces, ignoring the vast empty areas of the board.</li>
 * <li><b>Move Ordering:</b> Sorts candidate moves based on a shallow heuristic evaluation. Visiting promising moves first maximizes the efficiency of Alpha-Beta pruning.</li>
 * </ol>
 */
public class MinimaxAI {
    private final int aiPlayer;
    private final int opponent;
    private final int maxDepth;
    private final GameLogic logic;
    private final int boardSize;
    private final Evaluator evaluator;

    // A score representing a guaranteed win (must be larger than any heuristic score)
    private static final int WIN_SCORE = 1000000;

    // The radius around existing pieces to search for non-forcing moves
    private static final int MOVE_GENERATION_RADIUS = 2;

    // Performance metrics
    private int nodesEvaluated = 0;
    private int pruneCount = 0;

    /**
     * Internal class representing a move on the board.
     */
    private static class Move {
        final int r, c;
        Move(int r, int c) {
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

    /**
     * Helper class to associate a Move with its heuristic score for sorting.
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
     * Constructs the MinimaxAI instance.
     *
     * @param aiPlayer  The ID of the AI player.
     * @param opponent  The ID of the opponent (human).
     * @param maxDepth  The maximum depth of the search tree (ply).
     * @param logic     The game logic for verifying win conditions.
     * @param boardSize The size of the game board.
     */
    public MinimaxAI(int aiPlayer, int opponent, int maxDepth, GameLogic logic, int boardSize) {
        this.aiPlayer = aiPlayer;
        this.opponent = opponent;
        this.maxDepth = maxDepth;
        this.logic = logic;
        this.boardSize = boardSize;
        this.evaluator = new Evaluator(boardSize, aiPlayer, opponent);
        //this.threatSearch = new ThreatSpaceSearch(null, logic, boardSize);
    }

    /**
     * Calculates the best move for the AI in the current board state.
     *
     * @param board The current state of the game board.
     * @return An integer array {row, col} representing the best move.
     */
    public int[] findBestMove(Board board) {
        nodesEvaluated = 0;
        pruneCount = 0;
        long startTime = System.currentTimeMillis();

        // 1. Threat Search: Identify forced moves (wins or blocks)
        ThreatSpaceSearch ts = new ThreatSpaceSearch(board, logic, boardSize);
        List<ThreatSpaceSearch.Move> threats = ts.findThreats(aiPlayer, opponent);

        List<Move> possibleMoves;
        if (!threats.isEmpty()) {
            possibleMoves = new ArrayList<>();
            for (ThreatSpaceSearch.Move t : threats) {
                possibleMoves.add(new Move(t.r, t.c));
            }
            System.out.println("🎯 Threat space reduced to " + possibleMoves.size() + " moves");
        } else {
            // 2. If no immediate threats, generate relevant moves
            possibleMoves = getRelevantMoves(board);
        }

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        // 3. Immediate Win Check (Sanity check)
        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, aiPlayer);
            if (logic.checkWin(move.r, move.c, aiPlayer)) {
                board.setCell(move.r, move.c, Board.EMPTY);
                System.out.println("✓ Immediate win found!");
                return new int[]{move.r, move.c};
            }
            board.setCell(move.r, move.c, Board.EMPTY);
        }

        // 4. Move Ordering: Sort moves by a shallow heuristic check
        List<MoveScore> scoredMoves = new ArrayList<>();
        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, aiPlayer);
            scoredMoves.add(new MoveScore(move, evaluator.evaluate(board)));
            board.setCell(move.r, move.c, Board.EMPTY);
        }
        // Sort descending (best moves first)
        scoredMoves.sort(Comparator.comparingInt((MoveScore ms) -> ms.score).reversed());

        // 5. Deep Search (Minimax)
        for (MoveScore moveScore : scoredMoves) {
            Move move = moveScore.move;

            board.setCell(move.r, move.c, aiPlayer);
            // Call minimax for the next level (minimizing player's turn)
            int score = minimax(board, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, move.r, move.c);
            board.setCell(move.r, move.c, Board.EMPTY);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("📊 Nodes: " + nodesEvaluated +
                " | Pruned: " + pruneCount +
                " | Time: " + elapsed + "ms" +
                " | Score: " + bestScore);

        // Fallback if no moves found (e.g., board full or error)
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
     * Identifies cells that are worth exploring (neighbors of existing pieces).
     */
    private List<Move> getRelevantMoves(Board board) {
        Set<Move> moves = new HashSet<>();
        boolean hasAnyPiece = false;
        int pieceCount = 0;

        // Count pieces to potentially adjust radius (optimization)
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) != Board.EMPTY) {
                    pieceCount++;
                }
            }
        }

        // Dynamic radius: smaller search space in early game
        int radius = (pieceCount < 10) ? 1 : MOVE_GENERATION_RADIUS;

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) != Board.EMPTY) {
                    hasAnyPiece = true;

                    for (int dr = -radius; dr <= radius; dr++) {
                        for (int dc = -radius; dc <= radius; dc++) {
                            if (dr == 0 && dc == 0) continue;

                            int nr = r + dr;
                            int nc = c + dc;

                            if (board.isValid(nr, nc) && board.getCell(nr, nc) == Board.EMPTY) {
                                moves.add(new Move(nr, nc));
                            }
                        }
                    }
                }
            }
        }

        // If board is empty, start at the center
        if (!hasAnyPiece) {
            moves.add(new Move(boardSize / 2, boardSize / 2));
        }

        return new ArrayList<>(moves);
    }


    private int[] findFirstEmptyCell(Board board) {
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    return new int[]{r, c};
                }
            }
        }
        return new int[]{-1, -1};
    }


    /**
     * The recursive Minimax algorithm with Alpha-Beta Pruning.
     *
     * @param board              The current board state.
     * @param depth              Current depth in the search tree.
     * @param alpha              Alpha value (best already explored option along the path to the root for the maximizer).
     * @param beta               Beta value (best already explored option along the path to the root for the minimizer).
     * @param isMaximizingPlayer True if it's the AI's turn, False if it's the Opponent's turn.
     * @param lastR              The row of the last move made.
     * @param lastC              The column of the last move made.
     * @return The heuristic evaluation of the board.
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer, int lastR, int lastC) {
        nodesEvaluated++;

        // 1. Terminal State Check: Did the previous move result in a win?
        if (isMaximizingPlayer) {
            // Previous move was by Minimizer (Opponent)
            if (logic.checkWin(lastR, lastC, opponent)) {
                return -WIN_SCORE * (depth + 1); // Penalize loss (prefer losing later)
            }
        } else {
            // Previous move was by Maximizer (AI)
            if (logic.checkWin(lastR, lastC, aiPlayer)) {
                return WIN_SCORE * (depth + 1); // Reward win (prefer winning sooner)
            }
        }

        // 2. Depth Limit Reached
        if (depth == 0) {
            return evaluator.evaluate(board);
        }

        // 3. Move Generation (Threat-based or Relevant)
        ThreatSpaceSearch ts = new ThreatSpaceSearch(board, logic, boardSize);
        int currentPlayer = isMaximizingPlayer ? aiPlayer : opponent;
        int currentOpponent = isMaximizingPlayer ? opponent : aiPlayer;

        // Check for forcing moves first
        List<ThreatSpaceSearch.Move> threats = ts.findThreats(currentPlayer, currentOpponent);
        List<Move> possibleMoves;

        if (!threats.isEmpty()) {
            possibleMoves = new ArrayList<>();
            for (ThreatSpaceSearch.Move t : threats) {
                possibleMoves.add(new Move(t.r, t.c));
            }
        } else {
            possibleMoves = getRelevantMoves(board);
        }

        if (possibleMoves.isEmpty()) {
            return evaluator.evaluate(board);
        }

        // 4. Move Ordering
        List<MoveScore> scoredMoves = new ArrayList<>();
        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, currentPlayer);
            scoredMoves.add(new MoveScore(move, evaluator.evaluate(board)));
            board.setCell(move.r, move.c, Board.EMPTY); // Undo
        }

        if (isMaximizingPlayer) {
            scoredMoves.sort(Comparator.comparingInt((MoveScore ms) -> ms.score).reversed());
        } else {
            scoredMoves.sort(Comparator.comparingInt((MoveScore ms) -> ms.score));
        }

        // 5. Recursion
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
                    pruneCount++;
                    break; // Beta Cutoff
                }
            }
            return maxEval;
        }

        // Minimizing Player
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
                    pruneCount++;
                    break; // Alpha Cutoff
                }
            }
            return minEval;
        }
    }
}