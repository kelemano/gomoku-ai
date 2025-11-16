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
    private final ThreatSpaceSearch threatSearch;

    // A score far greater than any heuristic evaluation, used to represent a forced win.
    private static final int WIN_SCORE = 1000000;
    // The radius (in cells) around existing pieces to search for valid moves.
    private static final int MOVE_GENERATION_RADIUS = 2;

    private int nodesEvaluated = 0;
    private int pruneCount = 0;

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
        this.threatSearch = new ThreatSpaceSearch(null, logic, boardSize);
    }

    /**
     * The main public method to find the best move on the current board.
     * @param board The current board state.
     * @return An integer array [row, col] representing the best move.
     */
    public int[] findBestMove(Board board) {
        nodesEvaluated = 0;
        pruneCount = 0;

        long startTime = System.currentTimeMillis();

        // НОВОЕ: Сначала проверяем критические угрозы
        ThreatSpaceSearch ts = new ThreatSpaceSearch(board, logic, boardSize);
        List<ThreatSpaceSearch.Move> threats = ts.findThreats(aiPlayer, opponent);

        // Если есть форсирующие ходы, рассматриваем только их
        List<Move> possibleMoves;
        if (!threats.isEmpty()) {
            possibleMoves = new ArrayList<>();
            for (ThreatSpaceSearch.Move t : threats) {
                possibleMoves.add(new Move(t.r, t.c));
            }
            System.out.println("🎯 Threat space reduced to " + possibleMoves.size() + " moves");
        } else {
            // Иначе используем обычную генерацию ходов
            possibleMoves = getRelevantMoves(board);
        }

        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        // Быстрая проверка на немедленный выигрыш
        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, aiPlayer);
            if (logic.checkWin(move.r, move.c, aiPlayer)) {
                board.setCell(move.r, move.c, Board.EMPTY);
                System.out.println("✓ Immediate win found!");
                return new int[]{move.r, move.c};
            }
            board.setCell(move.r, move.c, Board.EMPTY);
        }

        // Сортируем ходы для лучшего pruning
        List<MoveScore> scoredMoves = new ArrayList<>();
        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, aiPlayer);
            scoredMoves.add(new MoveScore(move, evaluator.evaluate(board)));
            board.setCell(move.r, move.c, Board.EMPTY);
        }
        scoredMoves.sort(Comparator.comparingInt((MoveScore ms) -> ms.score).reversed());

        // Глубокий поиск
        for (MoveScore moveScore : scoredMoves) {
            Move move = moveScore.move;

            board.setCell(move.r, move.c, aiPlayer);
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
        int pieceCount = 0;

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                // If this cell is occupied, look at its neighbors
                if (board.getCell(r, c) != Board.EMPTY) {
                    pieceCount++;
                }
            }
        }

        // Адаптивный радиус: в начале игры - меньше, в середине - больше
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
        nodesEvaluated++;
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

        // Достигли глубины или ничья
        if (depth == 0) {
            return evaluator.evaluate(board);
        }

        // НОВОЕ: Генерируем ходы с учётом угроз
        ThreatSpaceSearch ts = new ThreatSpaceSearch(board, logic, boardSize);
        int currentPlayer = isMaximizingPlayer ? aiPlayer : opponent;
        int currentOpponent = isMaximizingPlayer ? opponent : aiPlayer;

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


        // 3. Move Ordering
        // Sort moves at this depth to maximize pruning efficiency.
        List<MoveScore> scoredMoves = new ArrayList<>();

        for (Move move : possibleMoves) {
            board.setCell(move.r, move.c, currentPlayer);
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
                    pruneCount++;
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
                    pruneCount++;
                    break; // Alpha Cutoff (Pruning)
                }
            }
            return minEval;
        }
    }
}