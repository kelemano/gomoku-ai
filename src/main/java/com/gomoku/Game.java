package com.gomoku;

import javafx.concurrent.Task;
import java.util.List;

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

    private Board board;
    private GameLogic logic;
    private MinimaxAI ai;

    private final GomokuGUI gui; // A reference to the GUI to send commands

    private boolean gameRunning;
    private int currentPlayer;
    private boolean aiIsThinking = false; // Prevents human from clicking while AI is busy

    /**
     * Constructor: Initializes the controller and the model.
     * @param gui A reference to the GomokuGUI (the View).
     */
    public Game(GomokuGUI gui) {
        this.gui = gui; // Store the reference to the GUI
        initializeGame();
    }

    private void initializeGame() {
        this.board = new Board(BOARD_SIZE);
        this.logic = new GameLogic(board, WIN_STREAK);
        this.ai = new MinimaxAI(AI_PLAYER, HUMAN_PLAYER, AI_DEPTH, logic, BOARD_SIZE);
        this.gameRunning = true;
        this.currentPlayer = HUMAN_PLAYER;
    }

    public void resetGame() {
        if (aiIsThinking) {
            return;
        }
        initializeGame();
        gui.clearBoard();
        gui.updateStatus("Your turn");

        System.out.println("Game reset");
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
                gameRunning = false;

                List<int[]> winningLine = logic.findWinningLine(r, c, HUMAN_PLAYER);
                gui.drawWinningLine(winningLine);

                gui.showGameEndMessage("You win");
            } else if (isBoardFull()) {
                gui.showGameEndMessage("Draw");
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
        gui.updateStatus("AI's turn · Thinking...");


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
                gameRunning = false;
                List<int[]> winningLine = logic.findWinningLine(r, c, AI_PLAYER);
                gui.drawWinningLine(winningLine);
                gui.showGameEndMessage("AI wins");
            } else if (isBoardFull()) {
                gui.showGameEndMessage("Draw");
                gameRunning = false;
            } else {
                // 4. Pass Turn to Human
                currentPlayer = HUMAN_PLAYER;
                gui.updateStatus("Your turn");
            }
            aiIsThinking = false;
        });

        aiMoveTask.setOnFailed(event -> {
            System.err.println("AI error: " + aiMoveTask.getException());
            gui.updateStatus("AI error · Your turn");
            aiIsThinking = false;
            currentPlayer = HUMAN_PLAYER;
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

    public boolean isGameRunning() {
        return gameRunning;
    }


    public boolean isAiThinking() {
        return aiIsThinking;
    }

}