package com.gomoku;

/**
 * The core class representing the Gomoku game board.
 * It stores the state of the grid and provides basic accessors and mutators.
 */
public class Board {

    // Constants
    private static final int DEFAULT_SIZE = 15; // Default board size is 15x15

    // Board state variables
    private final int size;
    private final int[][] grid; // 2D array to store cell states

    // Constants for cell states/player IDs
    public static final int EMPTY = 0;
    public static final int PLAYER_X = 1;
    public static final int PLAYER_O = 2;

    /**
     * Constructor that initializes the board to a specified size.
     * @param size The side length of the square board (N x N).
     */
    public Board(int size) {
        this.size = size;
        this.grid = new int[size][size];
    }

    /**
     * Default constructor, initializes a 15x15 board.
     */
    public Board() {
        this(DEFAULT_SIZE);
    }

    /**
     * @return The size of the board (side length).
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the state of a specific cell.
     * @param r Row index.
     * @param c Column index.
     * @return The cell state (EMPTY, PLAYER_X, or PLAYER_O).
     * @throws IllegalArgumentException if coordinates are out of bounds.
     */
    public int getCell(int r, int c) {
        if (isValid(r, c)) {
            return grid[r][c];
        }
        throw new IllegalArgumentException("Invalid coordinates: (" + r + ", " + c + ")");
    }

    /**
     * Attempts to place a player's piece at the specified cell OR undo a move.
     * @param r Row index.
     * @param c Column index.
     * @param player The ID of the player making the move (or EMPTY to undo).
     * @return true if the move was successfully made; false if the cell is already occupied (and not an undo).
     */
    public boolean setCell(int r, int c, int player) {
        if (!isValid(r, c)) {
            return false;
        }

        // *** ИСПРАВЛЕНИЕ ***
        // Позволяем ИИ "отменить" ход, установив ячейку обратно в EMPTY.
        if (player == EMPTY) {
            grid[r][c] = EMPTY;
            return true;
        }

        // Стандартная логика: можно ходить только в пустую ячейку
        if (grid[r][c] == EMPTY) {
            grid[r][c] = player;
            return true; // Move successfully placed
        }

        return false; // Cell is occupied
    }

    /**
     * Checks if the given coordinates are within the board boundaries.
     */
    public boolean isValid(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }

    /**
     * Prints the current state of the board to the console.
     */
    public void print() {
        System.out.print("   ");
        for (int c = 0; c < size; c++) {
            System.out.printf("%2d ", c);
        }
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.printf("%2d ", r);
            for (int c = 0; c < size; c++) {
                char symbol = switch (grid[r][c]) {
                    case PLAYER_X -> 'X';
                    case PLAYER_O -> 'O';
                    default -> '.';
                };
                System.out.print(" " + symbol + " ");
            }
            System.out.println();
        }
    }
}
