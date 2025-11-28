package com.gomoku;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Manages the "Welcome" experience of the application.
 * <p>
 * This class builds and manages the navigation between the main menu, rules,
 * customization settings, and the about screen. It uses a StackPane to swap
 * views with a fade animation.
 */
public class WelcomeScreen {

    private VBox root;
    private final Runnable onBegin;

    // UI Controls for Game Customization
    private ComboBox<String> playerShapeCombo;
    private ComboBox<String> playerColorCombo;
    private ComboBox<String> aiShapeCombo;
    private ComboBox<String> aiColorCombo;

    // Sub-screens
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
                "-fx-background-color: linear-gradient(to bottom right, #FBF8F4, #F5EDE4, #EFE6DB);"
        );

        mainMenuScreen = createMainMenu();
        rulesScreen = createRulesScreen();
        customizeScreen = createCustomizeScreen();
        aboutScreen = createAboutScreen();

        contentContainer.getChildren().add(mainMenuScreen);

        root = new VBox();
        root.getChildren().add(contentContainer);
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        contentContainer.prefWidthProperty().bind(root.widthProperty());
        contentContainer.prefHeightProperty().bind(root.heightProperty());
    }

    public VBox getRoot() {
        return root;
    }

    // ==========================================
    //              SCREEN CREATION
    // ==========================================

    private VBox createMainMenu() {
        VBox menu = new VBox();
        menu.setAlignment(Pos.CENTER);
        menu.setFillWidth(true);
        menu.getStyleClass().add("welcome-content");

        menu.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double height = menu.getHeight();
            double topBottom = Math.max(40, height * 0.08);
            double leftRight = Math.max(30, menu.getWidth() * 0.08);
            return new Insets(topBottom, leftRight, topBottom, leftRight);
        }, menu.heightProperty(), menu.widthProperty()));

        menu.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(30, menu.getHeight() * 0.06),
                menu.heightProperty()
        ));

        VBox headerBox = createHeader();
        Region headerDivider = createDecorativeDivider();

        VBox buttonsBox = new VBox();
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setFillWidth(true);
        buttonsBox.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(380, menu.getWidth() * 0.8),
                menu.widthProperty()
        ));

        buttonsBox.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(12, menu.getHeight() * 0.018),
                menu.heightProperty()
        ));

        Button playButton = createMenuButton("Play", true);
        playButton.setOnAction(e -> switchToScreen(customizeScreen));

        Button rulesButton = createMenuButton("Rules", false);
        rulesButton.setOnAction(e -> switchToScreen(rulesScreen));

        Button aboutButton = createMenuButton("About", false);
        aboutButton.setOnAction(e -> switchToScreen(aboutScreen));

        Button exitButton = createMenuButton("Exit", false);
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        buttonsBox.getChildren().addAll(playButton, rulesButton, aboutButton, exitButton);

        Label footer = new Label("— Strategy Game —");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #BEB0A7; -fx-letter-spacing: 3px;");

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        menu.getChildren().addAll(topSpacer, headerBox, headerDivider, buttonsBox, bottomSpacer, footer);
        return menu;
    }

    /**
     * Builds the Rules screen.
     */
    private VBox createRulesScreen() {
        VBox rulesContent = new VBox(20);
        rulesContent.setAlignment(Pos.TOP_CENTER);
        rulesContent.setFillWidth(true);
        rulesContent.getStyleClass().add("welcome-content");

        rulesContent.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double width = rulesContent.getWidth();
            double leftRight = Math.max(25, width * 0.06);
            return new Insets(25, leftRight, 25, leftRight);
        }, rulesContent.widthProperty()));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        VBox headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);

        Label title = new Label("How to Play");
        title.getStyleClass().add("screen-title");

        Label subtitle = new Label("Master the ancient art of five in a row");
        subtitle.getStyleClass().add("rules-subtitle");

        headerSection.getChildren().addAll(title, subtitle);

        VBox rulesBox = createBeautifulRulesSection();

        VBox examplesHeader = new VBox(5);
        examplesHeader.setAlignment(Pos.CENTER);
        examplesHeader.setPadding(new Insets(15, 0, 5, 0));

        Label examplesTitle = new Label("WINNING PATTERNS");
        examplesTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #B0A098; -fx-letter-spacing: 3px;");

        Region miniDivider = new Region();
        miniDivider.setPrefHeight(1);
        miniDivider.setMaxWidth(60);
        miniDivider.setStyle("-fx-background-color: #D4C4BC;");

        examplesHeader.getChildren().addAll(examplesTitle, miniDivider);

        FlowPane examplesBox = createExamplesSection();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("welcome-scroll-pane");

        VBox scrollContent = new VBox(20);
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.setPadding(new Insets(10, 0, 30, 0));
        scrollContent.getChildren().addAll(headerSection, rulesBox, examplesHeader, examplesBox);
        scrollPane.setContent(scrollContent);

        rulesContent.getChildren().addAll(topBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return rulesContent;
    }

    /**
     * Builds the Customization screen.
     */
    private VBox createCustomizeScreen() {
        VBox customContent = new VBox();
        customContent.setAlignment(Pos.CENTER);
        customContent.setFillWidth(true);
        customContent.getStyleClass().add("welcome-content");

        customContent.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double width = customContent.getWidth();
            double height = customContent.getHeight();
            double topBottom = Math.max(30, height * 0.05);
            double leftRight = Math.max(30, width * 0.08);
            return new Insets(topBottom, leftRight, topBottom, leftRight);
        }, customContent.widthProperty(), customContent.heightProperty()));

        customContent.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(25, customContent.getHeight() * 0.04),
                customContent.heightProperty()
        ));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        Label title = new Label("Customize Your Pieces");
        title.getStyleClass().add("screen-title");

        VBox customizationBox = createCustomizationSection();
        customizationBox.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(550, customContent.getWidth() * 0.9),
                customContent.widthProperty()
        ));

        setupColorProtection();

        Button playButton = new Button("START GAME");
        playButton.getStyleClass().add("primary-button");
        playButton.setOnAction(e -> {
            if (onBegin != null) {
                onBegin.run();
            }
        });

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        customContent.getChildren().addAll(topBar, topSpacer, title, customizationBox, playButton, bottomSpacer);
        return customContent;
    }

    /**
     * Prevents the user from selecting the same color for both players.
     * Automatically switches the other player's color if a conflict occurs.
     */
    private void setupColorProtection() {

        playerColorCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals(aiColorCombo.getValue())) {
                rotateColor(aiColorCombo, newVal);
            }
        });

        aiColorCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals(playerColorCombo.getValue())) {
                rotateColor(playerColorCombo, newVal);
            }
        });
    }

    /**
     * Helper to switch a ComboBox to the first available color that isn't forbidden.
     */
    private void rotateColor(ComboBox<String> target, String forbiddenColor) {
        for (String color : target.getItems()) {
            if (!color.equals(forbiddenColor)) {
                target.setValue(color);
                return;
            }
        }
    }

    /**
     * Builds the About screen with updated polished text.
     */
    private VBox createAboutScreen() {
        VBox aboutContent = new VBox();
        aboutContent.setAlignment(Pos.CENTER);
        aboutContent.setFillWidth(true);
        aboutContent.getStyleClass().add("welcome-content");

        aboutContent.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double width = aboutContent.getWidth();
            double height = aboutContent.getHeight();
            double topBottom = Math.max(40, height * 0.06);
            double leftRight = Math.max(40, width * 0.1);
            return new Insets(topBottom, leftRight, topBottom, leftRight);
        }, aboutContent.widthProperty(), aboutContent.heightProperty()));

        aboutContent.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(20, aboutContent.getHeight() * 0.03),
                aboutContent.heightProperty()
        ));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        Label title = new Label("About Gomoku");
        title.getStyleClass().add("screen-title");

        VBox description = new VBox(18);
        description.setAlignment(Pos.CENTER);
        description.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(500, aboutContent.getWidth() * 0.85),
                aboutContent.widthProperty()
        ));

        Label text1 = createAboutText("Gomoku (五目並べ) is a traditional Japanese strategy game requiring focus and foresight.");
        Label text2 = createAboutText("The objective is simple: be the first to form an unbroken line of five pieces.");

        Region divider = createDecorativeDivider();

        Label text3 = createAboutText("Challenge yourself against our smart AI opponent designed to test your tactical skills.");
        Label text4 = createAboutText("Find your flow. Connect five. Win.");

        Region divider2 = createDecorativeDivider();

        Label version = new Label("Version 1.0 · November 2025");
        version.getStyleClass().add("about-version");


        Label developer = new Label("Created by Olha Keleman with ♥");
        developer.getStyleClass().add("about-version");

        description.getChildren().addAll(text1, text2, divider, text3, text4, divider2, version, developer);

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        aboutContent.getChildren().addAll(topBar, topSpacer, title, description, bottomSpacer);
        return aboutContent;
    }

    // ==========================================
    //              HELPER METHODS
    // ==========================================

    private Label createAboutText(String content) {
        Label label = new Label(content);
        label.getStyleClass().add("about-text");
        label.setWrapText(true);
        return label;
    }

    private void switchToScreen(VBox newScreen) {
        if (contentContainer.getChildren().isEmpty()) {
            contentContainer.getChildren().add(newScreen);
            return;
        }

        VBox currentScreen = (VBox) contentContainer.getChildren().get(0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), currentScreen);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(newScreen);

            newScreen.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), newScreen);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private Button createMenuButton(String text, boolean primary) {
        Button button = new Button(text);
        if (primary) {
            button.getStyleClass().add("primary-button");
        } else {
            button.getStyleClass().add("secondary-button");
        }
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private Button createBackButton() {
        Button button = new Button("← Back");
        button.getStyleClass().add("back-button");
        return button;
    }

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

    private VBox createCustomizationSection() {
        VBox customBox = new VBox(20);
        customBox.setAlignment(Pos.CENTER);
        customBox.setFillWidth(true);

        VBox playerBox = createPlayerCustomization();
        VBox aiBox = createAiCustomization();

        customBox.getChildren().addAll(playerBox, aiBox);
        return customBox;
    }

    private VBox createPlayerCustomization() {
        return createPieceSelectionBox("Your Pieces", true);
    }

    private VBox createAiCustomization() {
        return createPieceSelectionBox("AI Pieces", false);
    }

    private VBox createPieceSelectionBox(String title, boolean isPlayer) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 25, 20, 25));
        box.getStyleClass().add("customize-card");

        Label label = new Label(title);
        label.getStyleClass().add("customize-label");

        FlowPane controls = new FlowPane();
        controls.setAlignment(Pos.CENTER);
        controls.setHgap(15);
        controls.setVgap(12);

        // Shape Selection
        HBox shapeBox = new HBox(8);
        shapeBox.setAlignment(Pos.CENTER);
        Label shapeLabel = new Label("Shape:");
        shapeLabel.getStyleClass().add("customize-sublabel");

        ComboBox<String> shapeCombo = new ComboBox<>();
        shapeCombo.getItems().addAll(
                "Circle", "Flat Stone", "Hexagon", "Diamond",
                "Star", "Heart", "Flower", "Rounded Square"
        );
        shapeCombo.setValue("Circle");

        if (isPlayer) this.playerShapeCombo = shapeCombo;
        else this.aiShapeCombo = shapeCombo;

        shapeBox.getChildren().addAll(shapeLabel, shapeCombo);

        // Color Selection
        HBox colorBox = new HBox(8);
        colorBox.setAlignment(Pos.CENTER);
        Label colorLabel = new Label("Color:");
        colorLabel.getStyleClass().add("customize-sublabel");

        ComboBox<String> colorCombo = new ComboBox<>();
        colorCombo.getItems().addAll(
                "Obsidian Black", "Charcoal Gray", "Slate Blue", "Espresso Brown",
                "Ruby Red", "Sapphire Blue", "Emerald Green",
                "Amethyst Purple", "Topaz Orange", "Turquoise",
                "Pearl White", "Ivory Cream", "Soft Beige", "Silver Gray",
                "Rose Pink", "Sky Blue", "Mint Green", "Lavender",
                "Peach", "Lemon Yellow", "Coral"
        );
        // Defaults
        colorCombo.setValue(isPlayer ? "Obsidian Black" : "Pearl White");

        if (isPlayer) this.playerColorCombo = colorCombo;
        else this.aiColorCombo = colorCombo;

        colorBox.getChildren().addAll(colorLabel, colorCombo);

        controls.getChildren().addAll(shapeBox, colorBox);
        box.getChildren().addAll(label, controls);

        return box;
    }

    private Region createDecorativeDivider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(180);
        divider.setMinWidth(100);
        divider.getStyleClass().add("zen-divider");
        return divider;
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(12);
        headerBox.setAlignment(Pos.CENTER);

        HBox piecesBox = new HBox(12);
        piecesBox.setAlignment(Pos.CENTER);

        Circle black = new Circle(10);
        black.getStyleClass().add("zen-piece-black");

        Circle white = new Circle(10);
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

    private VBox createBeautifulRulesSection() {
        VBox rulesBox = new VBox(12);
        rulesBox.setAlignment(Pos.CENTER);
        rulesBox.setMaxWidth(550);
        rulesBox.setPadding(new Insets(10, 0, 10, 0));

        HBox rule1 = createRuleCard("◑", "Take Turns", "Place your pieces alternately on the intersections of the board");
        HBox rule2 = createRuleCard("❺", "Five to Win", "Connect exactly five pieces in an unbroken row to claim victory");
        HBox rule3 = createRuleCard("✛", "Any Direction", "Horizontal, vertical, and diagonal lines all count as valid wins");
        HBox rule4 = createRuleCard("♟", "Challenge AI", "Test your skills against an intelligent computer opponent");

        rulesBox.getChildren().addAll(rule1, rule2, rule3, rule4);
        return rulesBox;
    }

    private HBox createRuleCard(String icon, String title, String description) {
        HBox card = new HBox(18);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.getStyleClass().add("rule-card");

        StackPane iconContainer = new StackPane();
        iconContainer.setMinWidth(50);
        iconContainer.setMinHeight(50);
        iconContainer.setMaxWidth(50);
        iconContainer.setMaxHeight(50);
        iconContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F5EDE8, #EBE0DA);" +
                        "-fx-background-radius: 12;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #C4A4A4;");
        iconContainer.getChildren().add(iconLabel);

        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #8B7B75;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 300; -fx-text-fill: #A09088;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(400);

        textBox.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        card.getChildren().addAll(iconContainer, textBox);
        return card;
    }

    private FlowPane createExamplesSection() {
        FlowPane examplesBox = new FlowPane();
        examplesBox.setAlignment(Pos.CENTER);
        examplesBox.setHgap(15);
        examplesBox.setVgap(15);
        examplesBox.setPadding(new Insets(10, 0, 10, 0));

        VBox horizontal = createZenExample("Horizontal", ExampleType.HORIZONTAL);
        VBox vertical = createZenExample("Vertical", ExampleType.VERTICAL);
        VBox diagonal1 = createZenExample("Diagonal ↘", ExampleType.DIAGONAL_DOWN);
        VBox diagonal2 = createZenExample("Diagonal ↗", ExampleType.DIAGONAL_UP);

        examplesBox.getChildren().addAll(horizontal, vertical, diagonal1, diagonal2);
        return examplesBox;
    }

    private enum ExampleType {
        HORIZONTAL, VERTICAL, DIAGONAL_DOWN, DIAGONAL_UP
    }

    private VBox createZenExample(String labelText, ExampleType type) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(14, 16, 14, 16));
        container.getStyleClass().add("example-card");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid.setVgap(2);
        grid.getStyleClass().add("zen-example-grid");

        int gridSize = 5;

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(24, 24);
                cell.setMinSize(24, 24);
                cell.setMaxSize(24, 24);
                cell.getStyleClass().add("zen-example-cell");

                boolean shouldHighlight = false;

                switch (type) {
                    case HORIZONTAL -> { if (r == 2) shouldHighlight = true; }
                    case VERTICAL -> { if (c == 2) shouldHighlight = true; }
                    case DIAGONAL_DOWN -> { if (r == c) shouldHighlight = true; }
                    case DIAGONAL_UP -> { if (r + c == 4) shouldHighlight = true; }
                }

                if (shouldHighlight) {
                    Circle piece = new Circle(8);
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