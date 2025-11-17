package com.gomoku;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.shape.Line;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import java.util.List;
import java.util.Comparator;

public class GomokuGUI extends Application {

    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;

    private Game game;
    private GridPane boardGrid;
    private Label statusLabel;
    private Button newGameButton;
    private Button menuButton;
    private Shape lastMoveMarker = null;

    private Pane drawingPane;
    private final StackPane[][] cellPanes = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private BorderPane root;
    private VBox gameEndOverlay;

    private Stage primaryStage;
    private Scene gameScene;
    private Scene welcomeScene;
    private WelcomeScreen welcomeScreen;

    // Piece customization settings
    private PieceSettings pieceSettings;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create the welcome screen content
        welcomeScreen = new WelcomeScreen(this::startGame);

        // Create welcome scene
        welcomeScene = new Scene(welcomeScreen.getRoot(), 700, 900);
        try {
            welcomeScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }

        primaryStage.setTitle("Gomoku · 五目並べ");
        primaryStage.setScene(welcomeScene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    /**
     * Called when user clicks "Begin" button
     */
    private void startGame() {
        System.out.println("Starting game...");

        // Get customization settings from welcome screen
        pieceSettings = new PieceSettings(
                welcomeScreen.getPlayerShape(),
                welcomeScreen.getPlayerColor(),
                welcomeScreen.getAiShape(),
                welcomeScreen.getAiColor()
        );

        // Create the game UI
        root = new BorderPane();

        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        // Create the game board in the center
        boardGrid = createBoardGrid();

        // Wrap board in StackPane to allow overlay
        StackPane boardContainer = new StackPane();
        boardContainer.getChildren().add(boardGrid);
        root.setCenter(boardContainer);

        // Create the Game Controller
        game = new Game(this);

        // Create and set the game scene
        gameScene = new Scene(root, (BOARD_SIZE * CELL_SIZE) + 80, (BOARD_SIZE * CELL_SIZE) + 160);
        try {
            gameScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }

        // Switch to game scene
        primaryStage.setTitle("Gomoku AI");
        primaryStage.setScene(gameScene);

        System.out.println("Game started!");
    }

    /**
     * Returns to main menu
     */
    private void returnToMenu() {
        primaryStage.setTitle("Gomoku · 五目並べ");
        primaryStage.setScene(welcomeScene);
    }

    /**
     * Creates the top panel with status and buttons
     */
    private HBox createTopPanel() {
        HBox topPanel = new HBox(20);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(20));
        topPanel.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        statusLabel = new Label("Your turn");
        statusLabel.getStyleClass().add("status-label");

        // Buttons container
        HBox buttonsBox = new HBox(12);
        buttonsBox.setAlignment(Pos.CENTER);

        newGameButton = new Button("New Game");
        newGameButton.getStyleClass().add("new-game-button");
        newGameButton.setOnAction(e -> {
            game.resetGame();
            hideGameEndOverlay();
        });

        // Menu button
        menuButton = new Button("Menu");
        menuButton.getStyleClass().add("menu-button-small");
        menuButton.setOnAction(e -> returnToMenu());

        buttonsBox.getChildren().addAll(newGameButton, menuButton);

        topPanel.getChildren().addAll(statusLabel, buttonsBox);
        return topPanel;
    }

    /**
     * Creates the 15x15 GridPane, filling it with StackPanes.
     */
    private GridPane createBoardGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("game-board");
        grid.setHgap(0);
        grid.setVgap(0);

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                StackPane cell = new StackPane();
                cell.getStyleClass().add("grid-cell");
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setMaxSize(CELL_SIZE, CELL_SIZE);

                // Add the click handler
                final int row = r;
                final int col = c;
                cell.setOnMouseClicked(event -> {
                    game.handleHumanTurn(row, col);
                });

                grid.add(cell, c, r);
                cellPanes[r][c] = cell;
            }
        }

        // Drawing pane for winning line
        drawingPane = new Pane();
        drawingPane.setMouseTransparent(true);
        drawingPane.setPrefSize(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE);
        grid.add(drawingPane, 0, 0, BOARD_SIZE, BOARD_SIZE);

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
            Shape piece;
            Color color;
            Color strokeColor;

            if (player == Board.PLAYER_X) {
                // Player piece
                piece = PieceSettings.createShape(pieceSettings.getPlayerShape(), CELL_SIZE / 2.0 - 5);
                color = pieceSettings.getPlayerColor();
                strokeColor = color.darker();
            } else {
                // AI piece
                piece = PieceSettings.createShape(pieceSettings.getAiShape(), CELL_SIZE / 2.0 - 5);
                color = pieceSettings.getAiColor();
                strokeColor = color.darker();
            }

            piece.setFill(color);
            piece.setStroke(strokeColor);
            piece.setStrokeWidth(1.5);

            // Remove last move highlight from previous piece
            if (lastMoveMarker != null) {
                lastMoveMarker.setStroke(((Color)lastMoveMarker.getFill()).darker());
                lastMoveMarker.setStrokeWidth(1.5);
            }

            // Add highlight to current piece
            piece.setStroke(Color.rgb(139, 115, 85));
            piece.setStrokeWidth(2.5);
            lastMoveMarker = piece;

            // Add a nice shadow
            piece.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.5)));

            // Add the piece to the correct cell on the grid
            cellPanes[r][c].getChildren().add(piece);
        });
    }

    public void clearBoard() {
        Platform.runLater(() -> {
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    cellPanes[r][c].getChildren().clear();
                    cellPanes[r][c].setStyle("");
                }
            }
            drawingPane.getChildren().clear();
            lastMoveMarker = null;
        });
    }

    public void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    /**
     * Shows a COMPACT game end notification
     */
    public void showGameEndMessage(String message) {
        Platform.runLater(() -> {
            hideGameEndOverlay(); // Remove any existing overlay

            // КОМПАКТНОЕ окно
            gameEndOverlay = new VBox(10);
            gameEndOverlay.setAlignment(Pos.CENTER);
            gameEndOverlay.getStyleClass().add("game-end-toast");
            gameEndOverlay.setMaxWidth(160);
            gameEndOverlay.setMaxHeight(120);
            gameEndOverlay.setPadding(new Insets(16, 20, 16, 20));

            // Result message
            Label resultLabel = new Label(message);
            resultLabel.setStyle(
                    "-fx-font-size: 15px;" +
                            "-fx-font-weight: 500;" +
                            "-fx-text-fill: #2D2D2D;" +
                            "-fx-letter-spacing: 0.5px;"
            );

            // Small divider line
            Pane divider = new Pane();
            divider.setPrefHeight(1);
            divider.setMaxWidth(40);
            divider.setStyle("-fx-background-color: rgba(45, 45, 45, 0.2);");

            // Compact button
            Button playAgainButton = new Button("New Game");
            playAgainButton.setStyle(
                    "-fx-background-color: #2D2D2D;" +
                            "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: 400;" +
                            "-fx-letter-spacing: 1px;" +
                            "-fx-padding: 7px 16px;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );
            playAgainButton.setOnAction(e -> {
                game.resetGame();
                hideGameEndOverlay();
            });

            gameEndOverlay.getChildren().addAll(resultLabel, divider, playAgainButton);

            // Add overlay to center
            StackPane boardContainer = (StackPane) root.getCenter();
            boardContainer.getChildren().add(gameEndOverlay);
            StackPane.setAlignment(gameEndOverlay, Pos.CENTER);

            // Animate appearance - fade and scale in
            gameEndOverlay.setOpacity(0);
            gameEndOverlay.setScaleX(0.8);
            gameEndOverlay.setScaleY(0.8);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), gameEndOverlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), gameEndOverlay);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1);
            scaleIn.setToY(1);

            fadeIn.play();
            scaleIn.play();
        });
    }

    /**
     * Hides the game end overlay
     */
    private void hideGameEndOverlay() {
        if (gameEndOverlay != null) {
            StackPane boardContainer = (StackPane) root.getCenter();
            boardContainer.getChildren().remove(gameEndOverlay);
            gameEndOverlay = null;
        }
    }

    /**
     * Elegant WIN effect - NO LINE, just glowing pieces
     */
    public void drawWinningLine(List<int[]> lineCoords) {
        if (lineCoords == null || lineCoords.size() < 2) return;

        lineCoords.sort(Comparator.comparingInt(coord -> coord[0]));

        if (lineCoords.get(0)[0] == lineCoords.get(lineCoords.size() - 1)[0]) {
            lineCoords.sort(Comparator.comparingInt(coord -> coord[1]));
        }

        Platform.runLater(() -> {
            // Animate winning pieces - elegant wave
            for (int i = 0; i < lineCoords.size(); i++) {
                int[] coord = lineCoords.get(i);
                int row = coord[0];
                int col = coord[1];

                StackPane cell = cellPanes[row][col];

                // Delayed wave effect
                PauseTransition pause = new PauseTransition(Duration.millis(i * 80));

                pause.setOnFinished(e -> {
                    if (!cell.getChildren().isEmpty()) {
                        Shape piece = (Shape) cell.getChildren().get(0);

                        // 1. Scale up animation
                        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), piece);
                        scaleUp.setToX(1.2);
                        scaleUp.setToY(1.2);
                        scaleUp.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

                        // 2. Add elegant soft glow
                        DropShadow glow = new DropShadow();
                        glow.setColor(Color.rgb(139, 115, 85, 0.6)); // Мягкий бежевый
                        glow.setRadius(20);
                        glow.setSpread(0.4);

                        piece.setEffect(glow);

                        // 3. Subtle pulsating glow (slower, more elegant)
                        Timeline pulse = new Timeline(
                                new KeyFrame(Duration.ZERO,
                                        new KeyValue(glow.radiusProperty(), 16)
                                ),
                                new KeyFrame(Duration.millis(1500),
                                        new KeyValue(glow.radiusProperty(), 24)
                                ),
                                new KeyFrame(Duration.millis(3000),
                                        new KeyValue(glow.radiusProperty(), 16)
                                )
                        );
                        pulse.setCycleCount(Timeline.INDEFINITE);

                        // 4. Very subtle background
                        FadeTransition bgFade = new FadeTransition(Duration.millis(400), cell);
                        cell.setStyle(
                                "-fx-background-color: rgba(139, 115, 85, 0.12);" +
                                        "-fx-background-radius: 8px;"
                        );

                        scaleUp.play();
                        pulse.play();
                    }
                });

                pause.play();
            }
        });
    }

}