package com.gomoku;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * This class manages the entire game flow:
 * - Initializes components (Board, Logic, AI)
 * - Contains the main Game Loop
 * - Handles player and AI turns
 * - Checks for win and draw conditions
 */
public class Game {

    // --- Game Settings ---
    private static final int BOARD_SIZE = 15;
    private static final int WIN_STREAK = 5;

    // Set AI depth (4 = fast, 6 = smarter, 8 = slow)
    private static final int AI_DEPTH = 4;

    private static final int HUMAN_PLAYER = Board.PLAYER_X;
    private static final int AI_PLAYER = Board.PLAYER_O;
    // ---------------------

    private final Board board;
    private final GameLogic logic;
    private final MinimaxAI ai;
    private final Scanner scanner;
    private boolean gameRunning;
    private int currentPlayer;

    /**
     * Constructor: Initializes all core components.
     */
    public Game() {
        this.board = new Board(BOARD_SIZE);
        this.logic = new GameLogic(board, WIN_STREAK);
        this.ai = new MinimaxAI(AI_PLAYER, HUMAN_PLAYER, AI_DEPTH, logic, BOARD_SIZE);
        this.scanner = new Scanner(System.in);
        this.gameRunning = true;
        this.currentPlayer = HUMAN_PLAYER; // Human plays first
    }

    /**
     * Starts the main game loop.
     */
    public void start() {
        System.out.println("--- GOMOKU ---");
        System.out.println("You are playing as 'X'. AI is playing as 'O'.");
        System.out.println("Win Condition: " + WIN_STREAK + " in a row.");
        //System.out.println("AI Depth: " + AI_DEPTH);

        // The Main Game Loop
        while (gameRunning) {

            if (isBoardFull()) {
                System.out.println("It's a draw! The board is full.");
                board.print();
                break;
            }

            if (currentPlayer == HUMAN_PLAYER) {
                handleHumanTurn();
            } else {
                handleAiTurn();
            }
        }

        System.out.println("Game Over.");
        scanner.close();
    }

    /**
     * Handles the human player's turn.
     */
    private void handleHumanTurn() {
        // Print the board *before* asking for a move
        board.print();
        System.out.println("\nYour turn (X).");
        int r = -1, c = -1;

        // Loop to get valid input
        while (true) {
            try {
                System.out.print("Enter row (0-" + (BOARD_SIZE - 1) + "): ");
                r = scanner.nextInt();
                System.out.print("Enter column (0-" + (BOARD_SIZE - 1) + "): ");
                c = scanner.nextInt();

                if (board.isValid(r, c) && board.getCell(r, c) == Board.EMPTY) {
                    board.setCell(r, c, HUMAN_PLAYER);
                    break; // Valid move
                } else {
                    System.out.println("Invalid move. Cell is occupied or out of bounds. Try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Error! Please enter a number.");
                scanner.next(); // Clear scanner buffer
            }
        }

        // *** CHANGE: Show the player's move immediately ***
        // Print the board *right after* the human's move,
        // *before* the win check and *before* the AI's turn.
        System.out.println("\nYou played at (" + r + ", " + c + "):");
        board.print();


        // *** KEY CHECK 1: Check for human win ***
        if (logic.checkWin(r, c, HUMAN_PLAYER)) {
            System.out.println("\n********************");
            System.out.println("    YOU WIN! (X)");
            System.out.println("********************");
            // board.print(); // Already printed above
            gameRunning = false; // End the game
        } else {
            currentPlayer = AI_PLAYER; // Pass the turn to the AI
        }
    }

    /**
     * Handles the AI's turn.
     */
    private void handleAiTurn() {
        System.out.println("\nAI's turn (O)... Thinking...");

        int[] aiMove = ai.findBestMove(board);
        int r = aiMove[0];
        int c = aiMove[1];

        if (r == -1 || !board.setCell(r, c, AI_PLAYER)) {
            System.out.println("AI Error: Cannot make a move.");
            gameRunning = false; // Emergency exit
            return;
        }

        System.out.println("AI moved to: (" + r + ", " + c + ")");

        // *** KEY CHECK 2: Check for AI win ***
        if (logic.checkWin(r, c, AI_PLAYER)) {
            System.out.println("\n********************");
            System.out.println("    AI WINS! (O)");
            System.out.println("********************");
            board.print(); // Show the AI's winning move
            gameRunning = false; // End the game
        } else {
            currentPlayer = HUMAN_PLAYER; // Pass the turn to the human
        }
    }

    /**
     * Helper method to check for a draw.
     * @return true if the board is full, false otherwise.
     */
    private boolean isBoardFull() {
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    return false; // Found an empty cell
                }
            }
        }
        return true; // No empty cells found
    }
}