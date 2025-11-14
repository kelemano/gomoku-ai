package com.gomoku;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.geometry.Insets;


public class GomokuGUI extends Application {

    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40; // Size of a cell in pixels

    private Game game; // The Controller
    private GridPane boardGrid; // The visual grid
    private Label statusLabel; // Shows "Your turn" or "AI is thinking"
    private Circle statusIcon;
    private Circle lastMoveMarker = null;

    // We need a 2D array of StackPanes to easily add pieces (Circles) to them
    private final StackPane[][] cellPanes = new StackPane[BOARD_SIZE][BOARD_SIZE];

    private Pane overlayPane;

    @Override
    public void start(Stage primaryStage) {
        // 1. Create the main layout
        BorderPane root = new BorderPane();

        // 2. Create the status label at the top
        statusLabel = new Label("Welcome to Gomoku! Your turn.");
        statusLabel.getStyleClass().add("status-label");
        root.setTop(statusLabel);
        BorderPane.setAlignment(statusLabel, Pos.CENTER);

        // 3. Create the game board in the center
        boardGrid = createBoardGrid();
        root.setCenter(boardGrid);

        // 4. Create the Game Controller
        game = new Game(this);

        // 5. Create and set the scene
        Scene scene = new Scene(root, (BOARD_SIZE * CELL_SIZE) + 40, (BOARD_SIZE * CELL_SIZE) + 80);

        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Gomoku AI");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Creates the 15x15 GridPane, filling it with StackPanes.
     */
    private GridPane createBoardGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("game-board");

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                StackPane cell = new StackPane();
                cell.getStyleClass().add("grid-cell");

                // Add the click handler
                final int row = r;
                final int col = c;
                cell.setOnMouseClicked(event -> {
                    // When clicked, tell the Game controller
                    game.handleHumanTurn(row, col);
                });

                grid.add(cell, c, r); // Add to (col, row)
                cellPanes[r][c] = cell; // Store it for later access
            }
        }
        return grid;
    }

    /**
     * This method is called BY THE GAME CONTROLLER to update the view.
     * @param r The row to draw on.
     * @param c The column to draw on.
     * @param player The player (PLAYER_X or PLAYER_O)
     */
    public void drawPiece(int r, int c, int player) {
        Platform.runLater(() -> {
            Circle piece = new Circle(CELL_SIZE / 2.0 - 5);

            if (player == Board.PLAYER_X) {
                piece.getStyleClass().add("player-x-piece");
            } else {
                piece.getStyleClass().add("player-o-piece");
            }

            if (lastMoveMarker != null) {
                lastMoveMarker.getStyleClass().remove("last-move-highlight");
            }

            piece.getStyleClass().add("last-move-highlight");

            lastMoveMarker = piece;

            // Add a nice shadow
            piece.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.5)));

            // Add the piece to the correct cell on the grid
            cellPanes[r][c].getChildren().add(piece);
        });
    }


    public void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
}


    /**
     * Called by the Game controller to show a final win/draw message.
     */
    public void showGameEndMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.setGraphic(null);

            DialogPane dialogPane = alert.getDialogPane();

            dialogPane.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm());

            dialogPane.getStyleClass().add("game-over-dialog");

            alert.showAndWait();
        });
    }
}