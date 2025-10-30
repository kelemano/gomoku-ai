package com.gomoku;

/**
 * Implements the Minimax algorithm with Alpha-Beta Pruning
 * to determine the optimal move for the AI player.
 */
public class MinimaxAI {
    private final int aiPlayer;
    private final int opponent;
    private final int maxDepth;
    private final GameLogic logic;
    private final int boardSize;
    private final Evaluator evaluator;

    /**
     * Constructor
     */
    public MinimaxAI(int aiPlayer, int opponent, int maxDepth, GameLogic logic, int boardSize) {
        this.aiPlayer = aiPlayer;
        this.opponent = opponent;
        this.maxDepth = maxDepth;
        this.logic = logic;
        this.boardSize = boardSize;
        this.evaluator = new Evaluator(logic, 5, boardSize);
    }

    /**
     * Public method to find the best move on the current board.
     * @param board The current state of the board.
     * @return An array of [row, column] representing the best move.
     */
    public int[] findBestMove(Board board) {
        // Initialize bestScore to the lowest possible value to ensure any real score is better
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = new int[]{-1, -1};

        // Iterate through all possible cells
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {

                if (board.getCell(r, c) == Board.EMPTY) {
                    // Make the test move on the board
                    board.setCell(r, c, aiPlayer);

                    // Call the recursive Minimax function
                    // We start by assuming the opponent will play next (isMaximizingPlayer=false)
                    // We initialize alpha and beta for the root node search.
                    int score = minimax(board, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

                    // Undo the move to restore the board state
                    board.setCell(r, c, Board.EMPTY);

                    // Update the best move found so far
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = r;
                        bestMove[1] = c;
                    }
                }
            }
        }

        // TODO: Implement a more robust check for when bestMove remains [-1, -1]
        return bestMove;
    }

    /**
     * Private recursive implementation of the Minimax algorithm with Alpha-Beta Pruning.
     * @param board The current board state.
     * @param depth The remaining search depth.
     * @param alpha The alpha cutoff value (for Maximizer).
     * @param beta The beta cutoff value (for Minimizer).
     * @param isMaximizingPlayer True if the current node is a maximizing player's turn.
     * @return The evaluation score for the current node.
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        // Base Cases (Terminal Conditions)

        // Check for Draw (Board Full)
        if (isBoardFull(board)) {
            return 0; // Score 0 for a draw
        }

        // If depth is 0, we must evaluate the board using the heuristic function
        if (depth == 0) {
            // We evaluate for the AI player
            return evaluator.evaluate(board, aiPlayer) - evaluator.evaluate(board, opponent);
        }


        // Maximizing Player (AI's Turn)
        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {

                    if (board.getCell(r, c) == Board.EMPTY) {

                        // Make the move (AI's move)
                        board.setCell(r, c, aiPlayer);

                        // Recursive Call: Evaluate the score of this new state (assuming opponent moves next)
                        int eval = minimax(board, depth - 1, alpha, beta, false);
                        maxEval = Math.max(maxEval, eval);

                        // Undo the move
                        board.setCell(r, c, Board.EMPTY);

                        // Alpha-Beta Pruning (Cutoff)
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) {
                            break; // Beta cutoff
                        }
                    }
                }
            }
            return maxEval;
        }

        // Minimizing Player (Opponent's Turn)
        else {
            int minEval = Integer.MAX_VALUE;

            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {

                    if (board.getCell(r, c) == Board.EMPTY) {

                        // Make the move (Opponent's move)
                        board.setCell(r, c, opponent);

                        // Recursive Call: Evaluate the score of this new state (assuming AI moves next)
                        int eval = minimax(board, depth - 1, alpha, beta, true);
                        minEval = Math.min(minEval, eval);

                        // Undo the move
                        board.setCell(r, c, Board.EMPTY);

                        // Alpha-Beta Pruning (Cutoff)
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) {
                            break; // Alpha cutoff
                        }
                    }
                }
            }
            return minEval;
        }
    }

    /**
     * Helper method to check if the board is completely full (a draw condition).
     * @param board The current state of the board.
     * @return True if the board is full, false otherwise.
     */
    private boolean isBoardFull(Board board) {
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    return false; // Found an empty cell, so not full
                }
            }
        }
        return true; // No empty cells found
    }
}
