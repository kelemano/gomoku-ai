package com.gomoku;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
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
    private static final int MARGIN = 20; // Отступ от краёв для сетки

    private Game game;
    private Pane boardPane; // Заменили GridPane на Pane
    private Label statusLabel;
    private Button newGameButton;
    private Button menuButton;
    private Shape lastMoveMarker = null;

    private Pane drawingPane;
    private final Shape[][] pieces = new Shape[BOARD_SIZE][BOARD_SIZE]; // Хранение фишек
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
        boardPane = createBoardPane();

        // Wrap board in StackPane to allow overlay
        StackPane boardContainer = new StackPane();
        boardContainer.getChildren().add(boardPane);
        root.setCenter(boardContainer);

        // Create the Game Controller
        game = new Game(this);

        // Create and set the game scene
        int totalSize = (BOARD_SIZE - 1) * CELL_SIZE + MARGIN * 2;
        gameScene = new Scene(root, totalSize + 80, totalSize + 160);
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
     * Преобразует координаты доски (row, col) в пиксельные координаты
     */
    private double[] getBoardCoordinates(int row, int col) {
        double x = MARGIN + col * CELL_SIZE;
        double y = MARGIN + row * CELL_SIZE;
        return new double[]{x, y};
    }
    private Pane createBoardPane() {
        int totalSize = (BOARD_SIZE - 1) * CELL_SIZE + MARGIN * 2;

        Pane pane = new Pane();
        pane.setPrefSize(totalSize, totalSize);
        pane.setMinSize(totalSize, totalSize);
        pane.setMaxSize(totalSize, totalSize);

        // ☕ Минималистичный стиль "Тёплый капучино"
        pane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FAF6F1, #F0E6D2);" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: #C9B39C;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
        );

        // Тонкие элегантные линии
        for (int i = 0; i < BOARD_SIZE; i++) {
            double x = MARGIN + i * CELL_SIZE;
            Line vLine = new Line(x, MARGIN, x, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
            vLine.setStroke(Color.rgb(139, 115, 85, 0.4)); // Тёплый коричневый
            vLine.setStrokeWidth(0.8);
            pane.getChildren().add(vLine);
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            double y = MARGIN + i * CELL_SIZE;
            Line hLine = new Line(MARGIN, y, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, y);
            hLine.setStroke(Color.rgb(139, 115, 85, 0.4));
            hLine.setStrokeWidth(0.8);
            pane.getChildren().add(hLine);
        }

        // Маленькие минималистичные звёздочки
        int[] starPoints = {3, 7, 11};
        for (int r : starPoints) {
            for (int c : starPoints) {
                double x = MARGIN + c * CELL_SIZE;
                double y = MARGIN + r * CELL_SIZE;
                javafx.scene.shape.Circle star = new javafx.scene.shape.Circle(x, y, 2.5);
                star.setFill(Color.rgb(139, 115, 85, 0.6));
                pane.getChildren().add(star);
            }
        }

        // Обработчик кликов
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
        drawingPane.setPrefSize(totalSize, totalSize);
        pane.getChildren().add(drawingPane);

        return pane;
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

            // Получаем координаты пересечения
            double[] coords = getBoardCoordinates(r, c);
            piece.setLayoutX(coords[0]);
            piece.setLayoutY(coords[1]);

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

            // Добавляем фишку на доску
            boardPane.getChildren().add(piece);
            pieces[r][c] = piece;
        });
    }


    public void clearBoard() {
        Platform.runLater(() -> {
            // Удаляем все фишки из массива
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    if (pieces[r][c] != null) {
                        boardPane.getChildren().remove(pieces[r][c]);
                        pieces[r][c] = null;
                    }
                }
            }

            // Очищаем drawingPane (линии победы)
            drawingPane.getChildren().clear();

            // ВАЖНО: Удаляем все фоновые круги и другие элементы
            // Оставляем только линии сетки, звёздочки и drawingPane
            boardPane.getChildren().removeIf(node -> {
                // Удаляем всё, кроме Line (линии сетки), маленьких Circle (звёздочки) и drawingPane
                if (node instanceof Line) {
                    return false; // Оставляем линии
                }
                if (node instanceof javafx.scene.shape.Circle) {
                    javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) node;
                    // Оставляем только маленькие круги (звёздочки radius=3.5)
                    return circle.getRadius() > 4; // Удаляем большие круги (фоновые подсветки)
                }
                if (node == drawingPane) {
                    return false; // Оставляем drawingPane
                }
                // Удаляем всё остальное (фишки, фоновые элементы)
                return true;
            });

            lastMoveMarker = null;
        });
    }

    public void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    /**
     * Компактная карточка в пудровом нюд стиле
     */
    public void showGameEndMessage(String message) {
        Platform.runLater(() -> {
            hideGameEndOverlay();

            // КОМПАКТНОЕ окно в пудровом стиле
            gameEndOverlay = new VBox(10);
            gameEndOverlay.setAlignment(Pos.CENTER);
            gameEndOverlay.setMaxWidth(160);
            gameEndOverlay.setMaxHeight(120);
            gameEndOverlay.setPadding(new Insets(16, 20, 16, 20));

            // Полупрозрачный пудровый фон
            gameEndOverlay.setStyle(
                    "-fx-background-color: rgba(250, 248, 245, 0.92);" + // Пудровый
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: rgba(168, 159, 145, 0.3);" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(92, 85, 82, 0.2), 15, 0, 0, 5);"
            );

            // Текст результата
            Label resultLabel = new Label(message);
            resultLabel.setStyle(
                    "-fx-font-size: 15px;" +
                            "-fx-font-weight: 500;" +
                            "-fx-text-fill: #5C5552;" + // Графитовый
                            "-fx-letter-spacing: 0.5px;"
            );

            // Разделитель
            Pane divider = new Pane();
            divider.setPrefHeight(1);
            divider.setMaxWidth(40);
            divider.setStyle("-fx-background-color: rgba(168, 159, 145, 0.25);");

            // Кнопка
            Button playAgainButton = new Button("New Game");
            playAgainButton.setStyle(
                    "-fx-background-color: #A89F91;" +
                            "-fx-text-fill: #FAF8F5;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: 400;" +
                            "-fx-letter-spacing: 1px;" +
                            "-fx-padding: 7px 16px;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );

            playAgainButton.setOnMouseEntered(e -> {
                playAgainButton.setStyle(
                        "-fx-background-color: #8F8679;" +
                                "-fx-text-fill: #FAF8F5;" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: 400;" +
                                "-fx-letter-spacing: 1px;" +
                                "-fx-padding: 7px 16px;" +
                                "-fx-background-radius: 12;" +
                                "-fx-cursor: hand;"
                );
            });

            playAgainButton.setOnMouseExited(e -> {
                playAgainButton.setStyle(
                        "-fx-background-color: #A89F91;" +
                                "-fx-text-fill: #FAF8F5;" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: 400;" +
                                "-fx-letter-spacing: 1px;" +
                                "-fx-padding: 7px 16px;" +
                                "-fx-background-radius: 12;" +
                                "-fx-cursor: hand;"
                );
            });

            playAgainButton.setOnAction(e -> {
                game.resetGame();
                hideGameEndOverlay();
            });

            gameEndOverlay.getChildren().addAll(resultLabel, divider, playAgainButton);

            StackPane boardContainer = (StackPane) root.getCenter();
            boardContainer.getChildren().add(gameEndOverlay);
            StackPane.setAlignment(gameEndOverlay, Pos.CENTER);

            // Анимация
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
     * Elegant WIN effect - glowing pieces with proper coordinates
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

                Shape piece = pieces[row][col];
                if (piece == null) continue;

                // Получаем координаты пересечения для фонового круга
                double[] coords = getBoardCoordinates(row, col);

                // Delayed wave effect
                PauseTransition pause = new PauseTransition(Duration.millis(i * 80));

                pause.setOnFinished(e -> {
                    // 1. Создаём фоновый круг-подсветку
                    javafx.scene.shape.Circle bgCircle = new javafx.scene.shape.Circle(
                            coords[0], coords[1], CELL_SIZE / 2.5
                    );
                    bgCircle.setFill(Color.rgb(139, 115, 85, 0.12));
                    bgCircle.setStroke(Color.TRANSPARENT);

                    // Добавляем фоновый круг под фишку
                    boardPane.getChildren().add(boardPane.getChildren().indexOf(piece), bgCircle);

                    // Анимация появления фона
                    FadeTransition bgFade = new FadeTransition(Duration.millis(400), bgCircle);
                    bgFade.setFromValue(0);
                    bgFade.setToValue(1);
                    bgFade.play();

                    // 2. Scale up animation для фишки
                    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(400), piece);
                    scaleUp.setToX(1.2);
                    scaleUp.setToY(1.2);
                    scaleUp.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

                    // 3. Add elegant soft glow
                    DropShadow glow = new DropShadow();
                    glow.setColor(Color.rgb(139, 115, 85, 0.6)); // Мягкий бежевый
                    glow.setRadius(20);
                    glow.setSpread(0.4);

                    piece.setEffect(glow);

                    // 4. Subtle pulsating glow (slower, more elegant)
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

                    scaleUp.play();
                    pulse.play();
                });

                pause.play();
            }
        });
    }

}