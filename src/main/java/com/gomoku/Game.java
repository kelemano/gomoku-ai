package com.gomoku;

import javafx.concurrent.Task;

/**
 * The Game Controller.
 * This class connects the GUI (GomokuGUI) to the game logic (Model).
 * It no longer uses the console.
 */
public class Game {


    private static final int BOARD_SIZE = 15;
    private static final int WIN_STREAK = 5;
    private static final int AI_DEPTH = 4;
    private static final int HUMAN_PLAYER = Board.PLAYER_X;
    private static final int AI_PLAYER = Board.PLAYER_O;

    private final Board board;
    private final GameLogic logic;
    private final MinimaxAI ai;

    private final GomokuGUI gui; // A reference to the GUI to send commands

    private boolean gameRunning;
    private int currentPlayer;
    private boolean aiIsThinking = false; // Prevents human from clicking while AI is busy

    /**
     * Constructor: Initializes the controller and the model.
     * @param gui A reference to the GomokuGUI (the View).
     */
    public Game(GomokuGUI gui) {
        this.board = new Board(BOARD_SIZE);
        this.logic = new GameLogic(board, WIN_STREAK);
        this.ai = new MinimaxAI(AI_PLAYER, HUMAN_PLAYER, AI_DEPTH, logic, BOARD_SIZE);
        this.gameRunning = true;
        this.currentPlayer = HUMAN_PLAYER;
        this.gui = gui; // Store the reference to the GUI
    }

    /**
     * This is the main entry point for a human's move, called by the GUI.
     * @param r The row clicked by the human.
     * @param c The column clicked by the human.
     */
    public void handleHumanTurn(int r, int c) {
        // Ignore click if game is over or if it's not the human's turn (e.g., AI is thinking)
        if (!gameRunning || currentPlayer != HUMAN_PLAYER || aiIsThinking) {
            return;
        }

        // Check if the move is valid
        if (board.isValid(r, c) && board.getCell(r, c) == Board.EMPTY) {
            // 1. Update Model
            board.setCell(r, c, HUMAN_PLAYER);

            // 2. Update View
            gui.drawPiece(r, c, HUMAN_PLAYER);

            // 3. Check Game State
            if (logic.checkWin(r, c, HUMAN_PLAYER)) {
                gui.showGameEndMessage("YOU WIN!");
                gameRunning = false;
            } else if (isBoardFull()) {
                gui.showGameEndMessage("IT'S A DRAW!");
                gameRunning = false;
            } else {
                // 4. Pass Turn to AI
                currentPlayer = AI_PLAYER;
                triggerAiTurn();
            }
        }
    }

    /**
     * Triggers the AI's move in a separate background thread
     * to prevent the GUI from freezing.
     */
    private void triggerAiTurn() {
        if (!gameRunning) return;

        aiIsThinking = true;
        gui.updateStatus("AI's turn! Thinking...");


        Task<int[]> aiMoveTask = new Task<>() {
            @Override
            protected int[] call() throws Exception {
                return ai.findBestMove(board);
            }
        };

        // This runs on the GUI thread *after* the background task is finished
        aiMoveTask.setOnSucceeded(event -> {
            int[] aiMove = aiMoveTask.getValue();
            int r = aiMove[0];
            int c = aiMove[1];

            // 1. Update Model
            board.setCell(r, c, AI_PLAYER);

            // 2. Update View
            gui.drawPiece(r, c, AI_PLAYER);

            // 3. Check Game State
            if (logic.checkWin(r, c, AI_PLAYER)) {
                gui.showGameEndMessage("AI WINS!");
                gameRunning = false;
            } else if (isBoardFull()) {
                gui.showGameEndMessage("IT'S A DRAW!");
                gameRunning = false;
            } else {
                // 4. Pass Turn to Human
                currentPlayer = HUMAN_PLAYER;
                gui.updateStatus("Your turn!");
            }
            aiIsThinking = false;
        });

        // Start the background thread
        new Thread(aiMoveTask).start();
    }

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
