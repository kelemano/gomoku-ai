package com.gomoku;

import javafx.application.Application;

/**
 * The entry point for the Gomoku game application.
 * <p>
 * This class is responsible for bootstrapping the JavaFX runtime and launching
 * the main graphical user interface defined in {@link GomokuGUI}.
 */
public class Main {
    /**
     * The main method serving as the application entry point.
     *
     * @param args Command-line arguments passed to the application (ignored in this implementation).
     */
    public static void main(String[] args) {
        Application.launch(GomokuGUI.class, args);
    }
}