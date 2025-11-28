package com.gomoku;

import javafx.concurrent.Task;
import java.util.List;

/**
 * The Game Controller.
 * <p>
 * This class acts as the bridge between the View (GomokuGUI) and the Model (Board, GameLogic, MinimaxAI).
 * It manages the game loop, handles turn switching between the human player and the AI,
 * and executes game state checks (win/loss/draw).
 */
public class Game {

    // === Game Constants ===
    private static final int BOARD_SIZE = 15;
    private static final int WIN_STREAK = 5;
    private static final int AI_DEPTH = 4; // Depth of the Minimax search tree

    // Player Identifiers
    private static final int HUMAN_PLAYER = Board.PLAYER_X;
    private static final int AI_PLAYER = Board.PLAYER_O;

    // === Model Components ===
    private Board board;
    private GameLogic logic;
    private MinimaxAI ai;

    // === View Component ===
    private final GomokuGUI gui;

    // === State Variables ===
    private boolean gameRunning;
    private int currentPlayer;

    /** Prevents the user from interacting with the board while the AI is calculating. */
    private boolean aiIsThinking = false;

    /**
     * Initializes the game controller.
     *
     * @param gui The reference to the main GUI class to send UI updates.
     */
    public Game(GomokuGUI gui) {
        this.gui = gui;
        initializeGame();
    }

    /**
     * Sets up the initial game state, creating the board and AI.
     */
    private void initializeGame() {
        this.board = new Board(BOARD_SIZE);
        this.logic = new GameLogic(board, WIN_STREAK);
        this.ai = new MinimaxAI(AI_PLAYER, HUMAN_PLAYER, AI_DEPTH, logic, BOARD_SIZE);
        this.gameRunning = true;
        this.currentPlayer = HUMAN_PLAYER;

        // Set initial status on the UI
        if (gui != null) {
            gui.updateStatus("Your turn", HUMAN_PLAYER);
        }
    }

    /**
     * Resets the game to its starting state.
     * Called when the user clicks "New Game" or "Play Again".
     */
    public void resetGame() {
        // Prevent reset if AI is currently calculating a move
        if (aiIsThinking) {
            return;
        }
        initializeGame();
        gui.clearBoard();
        gui.updateStatus("Your turn", HUMAN_PLAYER);
    }

    /**
     * Handles the interaction when the human player clicks a cell on the board.
     *
     * @param r The row index of the clicked cell.
     * @param c The column index of the clicked cell.
     */
    public void handleHumanTurn(int r, int c) {
        // Validation: Ignore clicks if game is over, it's not human turn, or AI is busy
        if (!gameRunning || currentPlayer != HUMAN_PLAYER || aiIsThinking) {
            return;
        }

        // Validate move validity
        if (board.isValid(r, c) && board.getCell(r, c) == Board.EMPTY) {
            // 1. Apply Human Move
            board.setCell(r, c, HUMAN_PLAYER);
            gui.drawPiece(r, c, HUMAN_PLAYER);

            // 2. Check Win/Draw conditions
            if (logic.checkWin(r, c, HUMAN_PLAYER)) {
                gameRunning = false;
                gui.updateStatus("", Board.EMPTY); // Clear status

                // Highlight winning line and show message
                List<int[]> winningLine = logic.findWinningLine(r, c, HUMAN_PLAYER);
                gui.drawWinningLine(winningLine);
                gui.showGameEndMessage("You win");

            } else if (isBoardFull()) {
                gameRunning = false;
                gui.updateStatus("", Board.EMPTY);
                gui.showGameEndMessage("Draw");
            } else {
                // 3. Switch turn to AI
                currentPlayer = AI_PLAYER;
                triggerAiTurn();
            }
        }
    }

    /**
     * Initiates the AI's move calculation in a background thread.
     * This ensures the JavaFX UI thread remains responsive (no freezing).
     */
    private void triggerAiTurn() {
        if (!gameRunning) return;

        aiIsThinking = true;
        gui.updateStatus("AI is thinking...", AI_PLAYER);

        // Create a background task for the AI calculation
        Task<int[]> aiMoveTask = new Task<>() {
            @Override
            protected int[] call() throws Exception {
                return ai.findBestMove(board);
            }
        };

        // Callback when AI finishes calculation successfully
        aiMoveTask.setOnSucceeded(event -> {
            int[] aiMove = aiMoveTask.getValue();
            int r = aiMove[0];
            int c = aiMove[1];

            // 1. Apply AI Move
            board.setCell(r, c, AI_PLAYER);
            gui.drawPiece(r, c, AI_PLAYER);

            // 2. Check Win/Draw conditions for AI
            if (logic.checkWin(r, c, AI_PLAYER)) {
                gameRunning = false;
                gui.updateStatus("", Board.EMPTY);

                List<int[]> winningLine = logic.findWinningLine(r, c, AI_PLAYER);
                gui.drawWinningLine(winningLine);
                gui.showGameEndMessage("AI wins");

            } else if (isBoardFull()) {
                gameRunning = false;
                gui.updateStatus("", Board.EMPTY);
                gui.showGameEndMessage("Draw");

            } else {
                // 3. Switch turn back to Human
                currentPlayer = HUMAN_PLAYER;
                gui.updateStatus("Your turn", HUMAN_PLAYER);
            }
            aiIsThinking = false;
        });

        // Callback if AI calculation fails (e.g., exception)
        aiMoveTask.setOnFailed(event -> {
            System.err.println("AI error: " + aiMoveTask.getException());
            gui.updateStatus("AI error · Your turn", HUMAN_PLAYER);
            aiIsThinking = false;
            currentPlayer = HUMAN_PLAYER;
        });
        new Thread(aiMoveTask).start();
    }

    /**
     * Checks if the board is completely full (Draw condition).
     *
     * @return true if no empty cells remain, false otherwise.
     */
    private boolean isBoardFull() {
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
}