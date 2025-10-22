package com.gomoku;

/**
 * Main class to demonstrate and test the Board and GameLogic classes.
 * This will serve as the initial console interface.
 */
public class Main {

    public static void main(String[] args) {
        int boardSize = 15;
        int winStreak = 5;

        // Initialize the core components
        Board board1 = new Board(boardSize);
        GameLogic logic = new GameLogic(board1, winStreak);

        System.out.println("--- Gomoku Game Core Test ---");
        System.out.println("Board Size: " + board1.getSize() + "x" + board1.getSize());
        System.out.println("Win Condition: " + winStreak + " in a row.\n");

        // Test Scenario 1: Horizontal Win for PLAYER_X (1)
        System.out.println("\n--- Testing Horizontal Win for X ---");

        // Player X (1) places 5 pieces in row 5, columns 5 through 9
        int lastR_H = 5;
        int lastC_H = 9;

        board1.setCell(5, 5, Board.PLAYER_X);
        board1.setCell(5, 6, Board.PLAYER_X);
        board1.setCell(5, 7, Board.PLAYER_X);
        board1.setCell(5, 8, Board.PLAYER_X);
        board1.setCell(lastR_H, lastC_H, Board.PLAYER_X);

        board1.print();

        boolean x1Wins = logic.checkWin(lastR_H, lastC_H, Board.PLAYER_X);
        System.out.println("\nDid PLAYER_X achieve a horizontal win? " + x1Wins);

        // Test Scenario 2: Vertical Win for PLAYER_O (2)
        System.out.println("\n--- Testing Vertical Win for O ---");

        Board board2 = new Board(boardSize);
        GameLogic logic2 = new GameLogic(board2, winStreak);

        // Player O (2) places 5 pieces in column 10, rows 10 through 14
        int lastR_V = 14;
        int lastC_V = 10;

        board2.setCell(10, 10, Board.PLAYER_O);
        board2.setCell(11, 10, Board.PLAYER_O);
        board2.setCell(12, 10, Board.PLAYER_O);
        board2.setCell(13, 10, Board.PLAYER_O);
        board2.setCell(lastR_V, lastC_V, Board.PLAYER_O);

        board2.print();

        boolean o1Wins = logic2.checkWin(lastR_V, lastC_V, Board.PLAYER_O);
        System.out.println("\nDid PLAYER_O achieve a vertical win? " + o1Wins);


        // Test Scenario 3: Main Diagonal Win for PLAYER_X (1)
        System.out.println("\n--- Testing Main Diagonal Win for X ---");

        Board board3 = new Board(boardSize);
        GameLogic logic3 = new GameLogic(board3, winStreak);

        int lastR_Md = 2;
        int lastC_Md = 2;

        board3.setCell(3,3, Board.PLAYER_X);
        board3.setCell(4,4, Board.PLAYER_X);
        board3.setCell(5,5, Board.PLAYER_X);
        board3.setCell(6,6,Board.PLAYER_X);
        board3.setCell(lastR_Md,  lastC_Md, Board.PLAYER_X);

        board3.print();

        boolean x2Wins = logic3.checkWin(lastR_Md,lastC_Md, Board.PLAYER_X);
        System.out.println("\nDid PLAYER_X achieve a main diagonal win? " + x2Wins);

        // Test Scenario 4: Anti-Diagonal Win for PLAYER_O (2)
        System.out.println("\n--- Testing Anti-Diagonal Win for O ---");

        Board board4 = new Board(boardSize);
        GameLogic logic4 = new GameLogic(board4, winStreak);

        int lastR_Ad = 0;
        int lastC_Ad = 4;

        board4.setCell(4, 0, Board.PLAYER_O);
        board4.setCell(3, 1, Board.PLAYER_O);
        board4.setCell(2, 2, Board.PLAYER_O);
        board4.setCell(1, 3, Board.PLAYER_O);
        board4.setCell(lastR_Ad, lastC_Ad, Board.PLAYER_O);

        board4.print();

        boolean o2Wins = logic4.checkWin(lastR_Ad, lastC_Ad, Board.PLAYER_O);
        System.out.println("\nDid PLAYER_O achieve an anti-diagonal win? " + o2Wins);

        // Test Scenario 5: No Win (Blocked Streak)
        System.out.println("\n--- Testing No Win Condition (4 in a row, blocked) ---");

        Board board5 = new Board(boardSize);
        GameLogic logic5 = new GameLogic(board5, winStreak);

        board5.setCell(8, 8, Board.PLAYER_X);
        board5.setCell(8, 9, Board.PLAYER_X);
        board5.setCell(8, 10, Board.PLAYER_X);
        board5.setCell(8, 11, Board.PLAYER_X);

        board5.setCell(8, 12, Board.PLAYER_O);

        int lastR_NoWin = 8;
        int lastC_NoWin = 11;

        board5.print();

        boolean xNoWin = logic5.checkWin(lastR_NoWin, lastC_NoWin, Board.PLAYER_X);
        System.out.println("\nDid PLAYER_X win with a blocked streak? " + xNoWin);

    }
}