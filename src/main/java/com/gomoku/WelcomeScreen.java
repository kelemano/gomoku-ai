package com.gomoku;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.application.Platform;

/**
 * WelcomeScreen - Multi-screen navigation system
 */
public class WelcomeScreen {

    private VBox root;
    private Runnable onBegin;

    // Customization options
    private ComboBox<String> playerShapeCombo;
    private ComboBox<String> playerColorCombo;
    private ComboBox<String> aiShapeCombo;
    private ComboBox<String> aiColorCombo;

    // Different screens
    private VBox mainMenuScreen;
    private VBox rulesScreen;
    private VBox customizeScreen;
    private VBox aboutScreen;
    private StackPane contentContainer;

    public WelcomeScreen(Runnable onBegin) {
        this.onBegin = onBegin;
        createScreens();
    }

    private void createScreens() {
        contentContainer = new StackPane();
        contentContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FAF8F5, #F0EBE3);" // Пудровый нюд
        );

        // Create all screens
        mainMenuScreen = createMainMenu();
        rulesScreen = createRulesScreen();
        customizeScreen = createCustomizeScreen();
        aboutScreen = createAboutScreen();

        // Start with main menu
        contentContainer.getChildren().add(mainMenuScreen);

        root = new VBox();
        root.getChildren().add(contentContainer);
    }

    public VBox getRoot() {
        return root;
    }

    /**
     * MAIN MENU - Beautiful title with buttons
     */
    private VBox createMainMenu() {
        VBox menu = new VBox(50);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(80, 50, 80, 50));
        menu.getStyleClass().add("welcome-content");

        // Header with beautiful title
        VBox headerBox = createHeader();

        // Menu buttons
        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setMaxWidth(350);

        Button playButton = createMenuButton("Play", "🎮", true);
        playButton.setOnAction(e -> switchToScreen(customizeScreen));

        Button rulesButton = createMenuButton("Rules", "📖", false);
        rulesButton.setOnAction(e -> switchToScreen(rulesScreen));

        Button aboutButton = createMenuButton("About", "ℹ️", false);
        aboutButton.setOnAction(e -> switchToScreen(aboutScreen));

        Button exitButton = createMenuButton("Exit", "🚪", false);
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        buttonsBox.getChildren().addAll(playButton, rulesButton, aboutButton, exitButton);

        menu.getChildren().addAll(headerBox, buttonsBox);
        return menu;
    }

    /**
     * RULES SCREEN - Shows game rules with examples
     */
    private VBox createRulesScreen() {
        VBox rulesContent = new VBox(20);
        rulesContent.setAlignment(Pos.TOP_CENTER);
        rulesContent.setPadding(new Insets(40, 50, 30, 50));
        rulesContent.getStyleClass().add("welcome-content");

        // Back button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        // Title
        Label title = new Label("How to Play");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 300; -fx-text-fill: #2D2D2D; -fx-letter-spacing: 3px; -fx-padding: 20 0 10 0;");

        // Rules
        VBox rulesBox = createRulesSection();

        // Divider
        Region divider = createDivider();

        // Examples
        FlowPane examplesBox = createExamplesSection();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("welcome-scroll-pane");

        VBox scrollContent = new VBox(20);
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.getChildren().addAll(title, rulesBox, divider, examplesBox);
        scrollPane.setContent(scrollContent);

        rulesContent.getChildren().addAll(topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return rulesContent;
    }

    /**
     * CUSTOMIZE SCREEN - Piece customization (accessed from Play button)
     */
    private VBox createCustomizeScreen() {
        VBox customContent = new VBox(35);
        customContent.setAlignment(Pos.CENTER);
        customContent.setPadding(new Insets(50, 50, 50, 50));
        customContent.getStyleClass().add("welcome-content");

        // Back button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        // Title
        Label title = new Label("Customize Your Pieces");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 300; -fx-text-fill: #2D2D2D; -fx-letter-spacing: 3px;");

        // Customization section
        VBox customizationBox = createCustomizationSection();

        // Play button
        Button playButton = new Button("START GAME");
        playButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2D2D2D, #1A1A1A);" +
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: 400;" +
                        "-fx-letter-spacing: 3px;" +
                        "-fx-padding: 16px 50px;" +
                        "-fx-background-radius: 25;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );
        playButton.setOnMouseEntered(e -> {
            playButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #1A1A1A, #000000);" +
                            "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 400;" +
                            "-fx-letter-spacing: 3px;" +
                            "-fx-padding: 16px 50px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);" +
                            "-fx-scale-x: 1.02;" +
                            "-fx-scale-y: 1.02;"
            );
        });
        playButton.setOnMouseExited(e -> {
            playButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #2D2D2D, #1A1A1A);" +
                            "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 400;" +
                            "-fx-letter-spacing: 3px;" +
                            "-fx-padding: 16px 50px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
            );
        });
        playButton.setOnAction(e -> {
            System.out.println("Begin button clicked!");
            System.out.println("Player: " + getPlayerShape() + " - " + getPlayerColor());
            System.out.println("AI: " + getAiShape() + " - " + getAiColor());
            if (onBegin != null) {
                onBegin.run();
            }
        });

        customContent.getChildren().addAll(topBar, title, customizationBox, playButton);
        return customContent;
    }

    /**
     * ABOUT SCREEN - Information about the game
     */
    private VBox createAboutScreen() {
        VBox aboutContent = new VBox(30);
        aboutContent.setAlignment(Pos.CENTER);
        aboutContent.setPadding(new Insets(60, 80, 60, 80));
        aboutContent.getStyleClass().add("welcome-content");

        // Back button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        // Title
        Label title = new Label("About Gomoku");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 300; -fx-text-fill: #2D2D2D; -fx-letter-spacing: 3px;");

        // Description
        VBox description = new VBox(20);
        description.setAlignment(Pos.CENTER);
        description.setMaxWidth(500);

        Label text1 = new Label("Gomoku (五目並べ) is an ancient Japanese strategy game,");
        text1.setStyle("-fx-font-size: 14px; -fx-text-fill: #5A5A5A; -fx-text-alignment: center;");
        text1.setWrapText(true);

        Label text2 = new Label("also known as Five in a Row or Gobang.");
        text2.setStyle("-fx-font-size: 14px; -fx-text-fill: #5A5A5A; -fx-text-alignment: center;");
        text2.setWrapText(true);

        Region divider = createDivider();

        Label text3 = new Label("This implementation features an intelligent AI");
        text3.setStyle("-fx-font-size: 14px; -fx-text-fill: #5A5A5A; -fx-text-alignment: center;");
        text3.setWrapText(true);

        Label text4 = new Label("using the Minimax algorithm with alpha-beta pruning.");
        text4.setStyle("-fx-font-size: 14px; -fx-text-fill: #5A5A5A; -fx-text-alignment: center;");
        text4.setWrapText(true);

        Region divider2 = createDivider();

        Label version = new Label("Version 1.0");
        version.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0B0B0; -fx-letter-spacing: 1px;");

        Label developer = new Label("Developed with ♥");
        developer.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0B0B0; -fx-letter-spacing: 1px;");

        description.getChildren().addAll(text1, text2, divider, text3, text4, divider2, version, developer);

        aboutContent.getChildren().addAll(topBar, title, description);
        return aboutContent;
    }

    /**
     * Switch between screens with fade animation
     */
    private void switchToScreen(VBox newScreen) {
        if (contentContainer.getChildren().isEmpty()) {
            contentContainer.getChildren().add(newScreen);
            return;
        }

        VBox currentScreen = (VBox) contentContainer.getChildren().get(0);

        // Fade out current screen
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentScreen);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(newScreen);

            // Fade in new screen
            newScreen.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newScreen);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Creates a beautiful menu button with soft rounded style
     */
    private Button createMenuButton(String text, String emoji, boolean primary) {
        Button button = new Button(emoji + "  " + text);

        if (primary) {
            // Primary button - dark gradient with glow
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #2D2D2D, #1A1A1A);" +
                            "-fx-text-fill: #FFFFFF;" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: 400;" +
                            "-fx-letter-spacing: 2px;" +
                            "-fx-padding: 18px 40px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);"
            );

            button.setOnMouseEntered(e -> {
                button.setStyle(
                        "-fx-background-color: linear-gradient(to right, #1A1A1A, #000000);" +
                                "-fx-text-fill: #FFFFFF;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: 400;" +
                                "-fx-letter-spacing: 2px;" +
                                "-fx-padding: 18px 40px;" +
                                "-fx-background-radius: 25;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 16, 0, 0, 6);" +
                                "-fx-scale-x: 1.02;" +
                                "-fx-scale-y: 1.02;"
                );
            });

            button.setOnMouseExited(e -> {
                button.setStyle(
                        "-fx-background-color: linear-gradient(to right, #2D2D2D, #1A1A1A);" +
                                "-fx-text-fill: #FFFFFF;" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: 400;" +
                                "-fx-letter-spacing: 2px;" +
                                "-fx-padding: 18px 40px;" +
                                "-fx-background-radius: 25;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);"
                );
            });
        } else {
            // Secondary buttons - light soft style
            button.setStyle(
                    "-fx-background-color: rgba(250, 250, 250, 0.9);" +
                            "-fx-text-fill: #2D2D2D;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: 300;" +
                            "-fx-letter-spacing: 2px;" +
                            "-fx-padding: 15px 40px;" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: rgba(45, 45, 45, 0.12);" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 25;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
            );

            button.setOnMouseEntered(e -> {
                button.setStyle(
                        "-fx-background-color: rgba(245, 245, 245, 1);" +
                                "-fx-text-fill: #1A1A1A;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: 300;" +
                                "-fx-letter-spacing: 2px;" +
                                "-fx-padding: 15px 40px;" +
                                "-fx-background-radius: 25;" +
                                "-fx-cursor: hand;" +
                                "-fx-border-color: rgba(45, 45, 45, 0.2);" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 25;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);" +
                                "-fx-scale-x: 1.01;" +
                                "-fx-scale-y: 1.01;"
                );
            });

            button.setOnMouseExited(e -> {
                button.setStyle(
                        "-fx-background-color: rgba(250, 250, 250, 0.9);" +
                                "-fx-text-fill: #2D2D2D;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: 300;" +
                                "-fx-letter-spacing: 2px;" +
                                "-fx-padding: 15px 40px;" +
                                "-fx-background-radius: 25;" +
                                "-fx-cursor: hand;" +
                                "-fx-border-color: rgba(45, 45, 45, 0.12);" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 25;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                );
            });
        }

        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /**
     * Creates a subtle back button
     */
    private Button createBackButton() {
        Button button = new Button("← Back");
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #6A6A6A;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 300;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: transparent;"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.03);" +
                            "-fx-text-fill: #2D2D2D;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: 300;" +
                            "-fx-padding: 8px 16px;" +
                            "-fx-cursor: hand;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: transparent;"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #6A6A6A;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: 300;" +
                            "-fx-padding: 8px 16px;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: transparent;"
            );
        });

        return button;
    }

    // Getters for customization options
    public String getPlayerShape() {
        return playerShapeCombo.getValue();
    }

    public String getPlayerColor() {
        return playerColorCombo.getValue();
    }

    public String getAiShape() {
        return aiShapeCombo.getValue();
    }

    public String getAiColor() {
        return aiColorCombo.getValue();
    }

    // Helper methods from previous version
    private VBox createCustomizationSection() {
        VBox customBox = new VBox(20);
        customBox.setAlignment(Pos.CENTER);
        customBox.setMaxWidth(500);

        VBox playerBox = createPlayerCustomization();
        VBox aiBox = createAiCustomization();

        customBox.getChildren().addAll(playerBox, aiBox);
        return customBox;
    }

    private VBox createPlayerCustomization() {
        VBox playerBox = new VBox(15);
        playerBox.setAlignment(Pos.CENTER);
        playerBox.setPadding(new Insets(20));
        playerBox.setStyle("-fx-background-color: rgba(139, 115, 85, 0.08); -fx-background-radius: 15;");

        Label playerLabel = new Label("Your Pieces");
        playerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 400; -fx-text-fill: #3A3A3A;");

        HBox playerControls = new HBox(20);
        playerControls.setAlignment(Pos.CENTER);

        Label shapeLabel = new Label("Shape:");
        shapeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A5A5A;");

        playerShapeCombo = new ComboBox<>();
        playerShapeCombo.getItems().addAll(
                "Circle", "Flat Stone", "Hexagon", "Diamond",
                "Star", "Heart", "Flower", "Rounded Square"
        );
        playerShapeCombo.setValue("Circle");

        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A5A5A;");

        playerColorCombo = new ComboBox<>();
        playerColorCombo.getItems().addAll(
                // Классические тёмные
                "Obsidian Black", "Charcoal Gray", "Slate Blue", "Espresso Brown",
                // Яркие тёмные
                "Ruby Red", "Sapphire Blue", "Emerald Green",
                "Amethyst Purple", "Topaz Orange", "Turquoise"
        );
        playerColorCombo.setValue("Obsidian Black");

        playerControls.getChildren().addAll(shapeLabel, playerShapeCombo, colorLabel, playerColorCombo);
        playerBox.getChildren().addAll(playerLabel, playerControls);

        return playerBox;
    }

    private VBox createAiCustomization() {
        VBox aiBox = new VBox(15);
        aiBox.setAlignment(Pos.CENTER);
        aiBox.setPadding(new Insets(20));
        aiBox.setStyle("-fx-background-color: rgba(139, 115, 85, 0.08); -fx-background-radius: 15;");

        Label aiLabel = new Label("AI Pieces");
        aiLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 400; -fx-text-fill: #3A3A3A;");

        HBox aiControls = new HBox(20);
        aiControls.setAlignment(Pos.CENTER);

        Label shapeLabel = new Label("Shape:");
        shapeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A5A5A;");

        aiShapeCombo = new ComboBox<>();
        aiShapeCombo.getItems().addAll(
                "Circle", "Flat Stone", "Hexagon", "Diamond",
                "Star", "Heart", "Flower", "Rounded Square"
        );
        aiShapeCombo.setValue("Circle");

        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5A5A5A;");

        aiColorCombo = new ComboBox<>();
        aiColorCombo.getItems().addAll(
                // Классические светлые
                "Pearl White", "Ivory Cream", "Soft Beige", "Silver Gray",
                // Яркие пастельные
                "Rose Pink", "Sky Blue", "Mint Green", "Lavender",
                "Peach", "Lemon Yellow", "Coral"
        );
        aiColorCombo.setValue("Pearl White");

        aiControls.getChildren().addAll(shapeLabel, aiShapeCombo, colorLabel, aiColorCombo);
        aiBox.getChildren().addAll(aiLabel, aiControls);

        return aiBox;
    }



    private Region createDivider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(200);
        divider.getStyleClass().add("zen-divider");
        return divider;
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);

        HBox piecesBox = new HBox(10);
        piecesBox.setAlignment(Pos.CENTER);

        Circle black = new Circle(8);
        black.getStyleClass().add("zen-piece-black");

        Circle white = new Circle(8);
        white.getStyleClass().add("zen-piece-white");

        piecesBox.getChildren().addAll(black, white);

        Label titleJp = new Label("五目並べ");
        titleJp.getStyleClass().add("title-japanese");

        Label titleEn = new Label("GOMOKU");
        titleEn.getStyleClass().add("title-english");

        Label subtitle = new Label("Five in a Row");
        subtitle.getStyleClass().add("subtitle-zen");

        headerBox.getChildren().addAll(piecesBox, titleJp, titleEn, subtitle);
        return headerBox;
    }

    private VBox createRulesSection() {
        VBox rulesBox = new VBox(10);
        rulesBox.setAlignment(Pos.CENTER);
        rulesBox.setMaxWidth(600);

        VBox rule1 = createZenRule("01", "Place pieces alternately on intersections");
        VBox rule2 = createZenRule("02", "Connect five pieces in a row to win");
        VBox rule3 = createZenRule("03", "Horizontal, vertical, or diagonal lines count");
        VBox rule4 = createZenRule("04", "Play against intelligent AI opponent");

        rulesBox.getChildren().addAll(rule1, rule2, rule3, rule4);
        return rulesBox;
    }

    private VBox createZenRule(String number, String text) {
        VBox ruleBox = new VBox(5);
        ruleBox.setAlignment(Pos.CENTER_LEFT);
        ruleBox.setPadding(new Insets(8, 0, 8, 0));

        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("zen-rule-number");
        numberLabel.setMinWidth(35);

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("zen-rule-text");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(500);

        contentBox.getChildren().addAll(numberLabel, textLabel);
        ruleBox.getChildren().add(contentBox);

        return ruleBox;
    }

    private FlowPane createExamplesSection() {
        FlowPane examplesBox = new FlowPane();
        examplesBox.setAlignment(Pos.CENTER);
        examplesBox.setHgap(15);
        examplesBox.setVgap(12);
        examplesBox.setPadding(new Insets(15, 0, 15, 0));

        VBox horizontal = createZenExample("Horizontal", ExampleType.HORIZONTAL);
        VBox vertical = createZenExample("Vertical", ExampleType.VERTICAL);
        VBox diagonal1 = createZenExample("Diagonal \\", ExampleType.DIAGONAL_DOWN);
        VBox diagonal2 = createZenExample("Diagonal /", ExampleType.DIAGONAL_UP);

        examplesBox.getChildren().addAll(horizontal, vertical, diagonal1, diagonal2);
        return examplesBox;
    }

    private enum ExampleType {
        HORIZONTAL, VERTICAL, DIAGONAL_DOWN, DIAGONAL_UP
    }

    private VBox createZenExample(String labelText, ExampleType type) {
        VBox container = new VBox(8);
        container.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(1.5);
        grid.setVgap(1.5);
        grid.getStyleClass().add("zen-example-grid");

        int gridSize = 5;

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(22, 22);
                cell.setMinSize(22, 22);
                cell.setMaxSize(22, 22);
                cell.getStyleClass().add("zen-example-cell");

                boolean shouldHighlight = false;

                switch (type) {
                    case HORIZONTAL:
                        if (r == 2) shouldHighlight = true;
                        break;
                    case VERTICAL:
                        if (c == 2) shouldHighlight = true;
                        break;
                    case DIAGONAL_DOWN:
                        if (r == c) shouldHighlight = true;
                        break;
                    case DIAGONAL_UP:
                        if (r + c == 4) shouldHighlight = true;
                        break;
                }

                if (shouldHighlight) {
                    Circle piece = new Circle(7);
                    piece.getStyleClass().add("zen-example-piece");
                    cell.getChildren().add(piece);
                }

                grid.add(cell, c, r);
            }
        }

        Label label = new Label(labelText);
        label.getStyleClass().add("zen-example-label");

        container.getChildren().addAll(grid, label);
        return container;
    }
}