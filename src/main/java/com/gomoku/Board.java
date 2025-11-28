package com.gomoku;

/**
 * Represents the Gomoku game board (grid model).
 *
 * This class stores the state of the board cells (Empty, Black, White)
 * and provides methods to validate coordinates, place pieces, and retrieve cell states.
 * It acts as the core data structure for the game logic and AI.
 */
public class Board {
    private final int size;
    private final int[][] grid;

    public static final int EMPTY = 0;
    public static final int PLAYER_X = 1;
    public static final int PLAYER_O = 2;

    /**
     * Constructs a new Board with the specified dimension.
     *
     * @param size The side length of the square board (e.g., 15).
     */
    public Board(int size) {
        this.size = size;
        this.grid = new int[size][size];
    }

    /**
     * Returns the size of the board.
     *
     * @return The side length of the board (N).
     */
    public int getSize() {
        return size;
    }

    /**
     * Retrieves the state of a specific cell.
     *
     * @param r The row index (0 to size-1).
     * @param c The column index (0 to size-1).
     * @return The state of the cell (Board.EMPTY, Board.PLAYER_X, or Board.PLAYER_O).
     * @throws IllegalArgumentException if the coordinates are outside the board boundaries.
     */
    public int getCell(int r, int c) {
        if (isValid(r, c)) {
            return grid[r][c];
        }
        throw new IllegalArgumentException("Invalid coordinates: (" + r + ", " + c + ")");
    }

    /**
     * Sets the state of a cell. This method is used for both making moves
     * and undoing moves (during AI calculation).
     *
     * @param r      The row index.
     * @param c      The column index.
     * @param player The value to set (Board.PLAYER_X, Board.PLAYER_O, or Board.EMPTY).
     * @return true if the operation was successful (cell was empty or we are clearing it),
     * false if the cell was already occupied by a piece (and we are not clearing it).
     */
    public boolean setCell(int r, int c, int player) {
        if (!isValid(r, c)) {
            return false;
        }

        // Case 1: Clearing a cell (Undo move)
        if (player == EMPTY) {
            grid[r][c] = EMPTY;
            return true;
        }

        // Case 2: Placing a piece (only allowed if cell is currently empty)
        if (grid[r][c] == EMPTY) {
            grid[r][c] = player;
            return true;
        }

        // Cell is occupied, invalid move
        return false;
    }

    /**
     * Checks if the provided coordinates are within the valid board boundaries.
     *
     * @param r The row index.
     * @param c The column index.
     * @return true if (r, c) is inside the grid, false otherwise.
     */
    public boolean isValid(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }
}