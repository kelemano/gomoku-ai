package com.gomoku;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;

import java.util.Comparator;
import java.util.List;

/**
 * The Main Application class for the Gomoku Game.
 * <p>
 * This class initializes the JavaFX application, manages the primary stage,
 * and handles the switching between the Welcome Screen and the Game Board.
 * It also handles all direct GUI updates (drawing pieces, animations, overlays).
 */

public class GomokuGUI extends Application {

    // === Board Constants ===
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int MARGIN = 20;

    // Calculated base size for the board pane
    private static final int BASE_BOARD_SIZE = (BOARD_SIZE - 1) * CELL_SIZE + MARGIN * 2;

    private Game game;
    private PieceSettings pieceSettings;

    // === GUI Elements ===
    private Stage primaryStage;
    private StackPane mainContainer; // Root container for view switching
    private BorderPane gameRoot; // Root layout for the active game
    private WelcomeScreen welcomeScreen;

    private Pane boardPane; // The visual board
    private Pane drawingPane; // Layer for drawing winning lines
    private Label statusLabel; // "Your turn" / "AI Thinking"
    private VBox gameEndOverlay; // Win/Loss popup

    private final Shape[][] pieces = new Shape[BOARD_SIZE][BOARD_SIZE];
    private Shape lastMoveMarker = null;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Root container that holds either the WelcomeScreen or the GameRoot
        mainContainer = new StackPane();
        mainContainer.getStyleClass().add("root");

        // Initialize Welcome Screen
        welcomeScreen = new WelcomeScreen(this::startGame);

        // Show Welcome Screen initially
        mainContainer.getChildren().add(welcomeScreen.getRoot());

        // Setup Scene
        Scene scene = new Scene(mainContainer, 900, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }

        primaryStage.setTitle("Gomoku · 五目並べ");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(650);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    /**
     * Smoothly transitions content within the main window (e.g., Menu -> Game).
     */
    private void switchView(Parent newContent) {
        newContent.setOpacity(0);
        mainContainer.getChildren().add(newContent);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeIn.setOnFinished(e -> {
            // Remove the old view to free up resources
            if (mainContainer.getChildren().size() > 1) {
                mainContainer.getChildren().remove(0);
            }
        });
        fadeIn.play();
    }

    /**
     * Initializes and starts a new game session.
     * Called when "Start Game" is clicked in the Welcome Screen.
     */
    private void startGame() {
        System.out.println("Starting game...");

        // Capture settings from the welcome screen
        pieceSettings = new PieceSettings(
                welcomeScreen.getPlayerShape(),
                welcomeScreen.getPlayerColor(),
                welcomeScreen.getAiShape(),
                welcomeScreen.getAiColor()
        );

        // Build the Game Interface
        gameRoot = new BorderPane();
        gameRoot.setStyle("-fx-background-color: transparent;");

        // Top Panel (Status & Buttons)
        BorderPane topPanel = createTopPanel();
        gameRoot.setTop(topPanel);

        // Game Board
        boardPane = createBoardPane();

        // Wrap board for responsive scaling
        StackPane boardWrapper = new StackPane(boardPane);
        boardWrapper.setPadding(new Insets(20));

        // Scale transform for the board
        Scale scale = new Scale(1, 1);
        boardPane.getTransforms().add(scale);

        // Responsive scaling logic
        boardWrapper.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double availableWidth = newBounds.getWidth() - 40;
            double availableHeight = newBounds.getHeight() - 40;

            double scaleFactor = Math.min(
                    availableWidth / BASE_BOARD_SIZE,
                    availableHeight / BASE_BOARD_SIZE
            );
            // Limit zoom levels
            scaleFactor = Math.max(0.5, Math.min(scaleFactor, 1.5));

            scale.setPivotX(BASE_BOARD_SIZE / 2.0);
            scale.setPivotY(BASE_BOARD_SIZE / 2.0);
            scale.setX(scaleFactor);
            scale.setY(scaleFactor);
        });

        gameRoot.setCenter(boardWrapper);

        // Start Logic
        game = new Game(this);

        switchView(gameRoot);
    }

    private void returnToMenu() {
        switchView(welcomeScreen.getRoot());
    }

    /**
     * Creates the top navigation bar with the status label centered.
     */
    private BorderPane createTopPanel() {
        BorderPane topPanel = new BorderPane();
        topPanel.setPadding(new Insets(15, 25, 15, 25));
        topPanel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.6); -fx-border-color: #D4C4BC; -fx-border-width: 0 0 1 0;");

        // 1. Status Label (Center)
        statusLabel = new Label("Your turn");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 500; -fx-text-fill: #8B7B75;");

        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPickOnBounds(false);

        // 2. Control Buttons (Right)
        HBox buttonsBox = new HBox(12);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button newGameButton = new Button("New Game");
        newGameButton.getStyleClass().add("new-game-button");
        newGameButton.setOnAction(e -> {
            game.resetGame();
            hideGameEndOverlay();
        });

        Button menuButton = new Button("Menu");
        menuButton.getStyleClass().add("menu-button-small");
        menuButton.setOnAction(e -> returnToMenu());

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("menu-button-small");
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        buttonsBox.getChildren().addAll(newGameButton, menuButton, exitButton);

        // 3. Layout
        StackPane layout = new StackPane();
        layout.getChildren().addAll(statusBox, buttonsBox);
        StackPane.setAlignment(statusBox, Pos.CENTER);
        StackPane.setAlignment(buttonsBox, Pos.CENTER_RIGHT);

        topPanel.setCenter(layout);

        return topPanel;
    }

    /**
     * Generates the grid visual.
     */
    private Pane createBoardPane() {
        Pane pane = new Pane();
        pane.setPrefSize(BASE_BOARD_SIZE, BASE_BOARD_SIZE);
        pane.setMinSize(BASE_BOARD_SIZE, BASE_BOARD_SIZE);
        pane.setMaxSize(BASE_BOARD_SIZE, BASE_BOARD_SIZE);

        pane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FAF6F1, #F0E6D2);" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #C9B39C;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 4);"
        );

        // Draw grid lines
        for (int i = 0; i < BOARD_SIZE; i++) {
            double x = MARGIN + i * CELL_SIZE;
            Line vLine = new Line(x, MARGIN, x, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
            vLine.setStroke(Color.rgb(139, 115, 85, 0.4));
            vLine.setStrokeWidth(0.8);
            pane.getChildren().add(vLine);

            double y = MARGIN + i * CELL_SIZE;
            Line hLine = new Line(MARGIN, y, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, y);
            hLine.setStroke(Color.rgb(139, 115, 85, 0.4));
            hLine.setStrokeWidth(0.8);
            pane.getChildren().add(hLine);
        }

        // Draw star points (hoshi)
        int[] starPoints = {3, 7, 11};
        for (int r : starPoints) {
            for (int c : starPoints) {
                double x = MARGIN + c * CELL_SIZE;
                double y = MARGIN + r * CELL_SIZE;
                javafx.scene.shape.Circle star = new javafx.scene.shape.Circle(x, y, 3);
                star.setFill(Color.rgb(139, 115, 85, 0.6));
                pane.getChildren().add(star);
            }
        }
        // Handle clicks
        pane.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            int col = (int) Math.round((mouseX - MARGIN) / CELL_SIZE);
            int row = (int) Math.round((mouseY - MARGIN) / CELL_SIZE);
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                game.handleHumanTurn(row, col);
            }
        });

        drawingPane = new Pane();
        drawingPane.setMouseTransparent(true);
        drawingPane.setPrefSize(BASE_BOARD_SIZE, BASE_BOARD_SIZE);
        pane.getChildren().add(drawingPane);

        return pane;
    }

    private double[] getBoardCoordinates(int row, int col) {
        double x = MARGIN + col * CELL_SIZE;
        double y = MARGIN + row * CELL_SIZE;
        return new double[]{x, y};
    }

    /**
     * Draws a piece on the board.
     */
    public void drawPiece(int r, int c, int player) {
        Platform.runLater(() -> {
            Shape piece;
            Color color;
            Color strokeColor;

            if (player == Board.PLAYER_X) {
                piece = PieceSettings.createShape(pieceSettings.getPlayerShape(), CELL_SIZE / 2.0 - 5);
                color = pieceSettings.getPlayerColor();
            } else {
                piece = PieceSettings.createShape(pieceSettings.getAiShape(), CELL_SIZE / 2.0 - 5);
                color = pieceSettings.getAiColor();
            }
            strokeColor = color.darker();

            piece.setFill(color);
            piece.setStroke(strokeColor);
            piece.setStrokeWidth(1.5);

            double[] coords = getBoardCoordinates(r, c);
            piece.setLayoutX(coords[0]);
            piece.setLayoutY(coords[1]);

            // Update last move highlight
            if (lastMoveMarker != null) {
                lastMoveMarker.setStroke(((Color) lastMoveMarker.getFill()).darker());
                lastMoveMarker.setStrokeWidth(1.5);
            }

            piece.setStroke(Color.rgb(139, 115, 85));
            piece.setStrokeWidth(2.5);
            lastMoveMarker = piece;

            piece.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.2)));

            boardPane.getChildren().add(piece);
            pieces[r][c] = piece;
        });
    }

    /**
     * Clears the board for a new game.
     */
    public void clearBoard() {
        Platform.runLater(() -> {
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    pieces[r][c] = null;
                }
            }

            drawingPane.getChildren().clear();

            // Clear visual nodes, preserving grid lines and star points
            boardPane.getChildren().removeIf(node -> {
                if (node instanceof Line) return false;
                if (node == drawingPane) return false;
                // Preserve star points (small circles)
                if (node instanceof javafx.scene.shape.Circle) {
                    double radius = ((javafx.scene.shape.Circle) node).getRadius();
                    if (radius < 5) {
                        return false;
                    }
                }
                return true;
            });

            lastMoveMarker = null;
        });
    }

    /**
     * Updates the status text and player icon.
     */
    public void updateStatus(String text, int activePlayerId) {
        Platform.runLater(() -> {
            statusLabel.setText(text);

            if (pieceSettings == null || activePlayerId == Board.EMPTY) {
                statusLabel.setGraphic(null);
                return;
            }

            Shape icon;
            Color color;

            if (activePlayerId == Board.PLAYER_X) {
                icon = PieceSettings.createShape(pieceSettings.getPlayerShape(), 7);
                color = pieceSettings.getPlayerColor();
            } else {
                icon = PieceSettings.createShape(pieceSettings.getAiShape(), 7);
                color = pieceSettings.getAiColor();
            }

            icon.setFill(color);
            icon.setStroke(color.darker());
            icon.setStrokeWidth(1.0);
            icon.setEffect(new DropShadow(2, Color.rgb(0, 0, 0, 0.2)));

            statusLabel.setGraphic(icon);
            statusLabel.setGraphicTextGap(12);
        });
    }

    /**
     * Displays the game end result popup (Win/Loss/Draw).
     */
    public void showGameEndMessage(String message) {
        Platform.runLater(() -> {
            hideGameEndOverlay();

            gameEndOverlay = new VBox(15);
            gameEndOverlay.setAlignment(Pos.CENTER);

            gameEndOverlay.setMinWidth(220);
            gameEndOverlay.setMaxWidth(220);
            gameEndOverlay.setMinHeight(140);
            gameEndOverlay.setMaxHeight(140);
            gameEndOverlay.setPadding(new Insets(20));

            gameEndOverlay.setStyle(
                    "-fx-background-color: rgba(255, 252, 250, 0.85);" +
                            "-fx-background-radius: 30;" +
                            "-fx-border-radius: 30;" +
                            "-fx-border-color: rgba(224, 213, 207, 0.6);" +
                            "-fx-border-width: 1.5;" +
                            "-fx-effect: dropshadow(gaussian, rgba(139, 123, 123, 0.15), 15, 0, 0, 4);"
            );

            Label resultLabel = new Label(message);
            resultLabel.setStyle(
                    "-fx-font-family: 'SF Pro Display', sans-serif;" +
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: 600;" +
                            "-fx-text-fill: #8B7B75;"
            );

            Button playAgainButton = new Button("Play Again");
            playAgainButton.getStyleClass().add("primary-button");
            playAgainButton.setPrefWidth(150);
            playAgainButton.setStyle("-fx-font-size: 13px; -fx-padding: 8px 16px;");

            playAgainButton.setOnAction(e -> {
                game.resetGame();
                hideGameEndOverlay();
            });

            gameEndOverlay.getChildren().addAll(resultLabel, playAgainButton);
            gameEndOverlay.setLayoutX((BASE_BOARD_SIZE - 220) / 2.0);
            gameEndOverlay.setLayoutY((BASE_BOARD_SIZE - 140) / 2.0);
            boardPane.getChildren().add(gameEndOverlay);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), gameEndOverlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), gameEndOverlay);
            scaleUp.setFromX(0.85);
            scaleUp.setFromY(0.85);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            fadeIn.play();
            scaleUp.play();
        });
    }

    private void hideGameEndOverlay() {
        if (gameEndOverlay != null) {
            boardPane.getChildren().remove(gameEndOverlay);
            gameEndOverlay = null;
        }
    }

    /**
     * Animates the winning pieces.
     */
    public void drawWinningLine(List<int[]> lineCoords) {
        if (lineCoords == null || lineCoords.size() < 2) return;

        lineCoords.sort(Comparator.comparingInt(c -> c[0] * BOARD_SIZE + c[1]));

        Platform.runLater(() -> {
            for (int i = 0; i < lineCoords.size(); i++) {
                int[] coord = lineCoords.get(i);
                int r = coord[0];
                int c = coord[1];
                Shape piece = pieces[r][c];
                if (piece == null) continue;

                PauseTransition pause = new PauseTransition(Duration.millis(i * 100));
                pause.setOnFinished(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(300), piece);
                    st.setToX(1.3);
                    st.setToY(1.3);
                    st.play();
                });
                pause.play();
            }
        });
    }
}