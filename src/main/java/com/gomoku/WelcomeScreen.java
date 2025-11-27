package com.gomoku;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx. geometry.Insets;
import javafx. geometry.Pos;
import javafx.scene.control.Button;
import javafx. scene.control.Label;
import javafx.scene.control. ScrollPane;
import javafx.scene.control.ComboBox;
import javafx.scene. layout.*;
import javafx. scene.shape.Circle;
import javafx.util.Duration;
import javafx.application.Platform;

/**
 * WelcomeScreen - Adaptive multi-screen navigation with pastel beige & rose theme
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
                "-fx-background-color: linear-gradient(to bottom right, #FBF8F4, #F5EDE4, #EFE6DB);"
        );

        // Create all screens
        mainMenuScreen = createMainMenu();
        rulesScreen = createRulesScreen();
        customizeScreen = createCustomizeScreen();
        aboutScreen = createAboutScreen();

        // Start with main menu
        contentContainer.getChildren(). add(mainMenuScreen);

        root = new VBox();
        root.getChildren().add(contentContainer);
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        // Make contentContainer fill available space
        contentContainer.prefWidthProperty().bind(root.widthProperty());
        contentContainer.prefHeightProperty().bind(root.heightProperty());
    }

    public VBox getRoot() {
        return root;
    }

    /**
     * MAIN MENU - Clean design without emojis
     */
    private VBox createMainMenu() {
        VBox menu = new VBox();
        menu.setAlignment(Pos.CENTER);
        menu. setFillWidth(true);
        menu.getStyleClass().add("welcome-content");

        // Adaptive padding
        menu.paddingProperty().bind(Bindings. createObjectBinding(() -> {
            double height = menu.getHeight();
            double topBottom = Math.max(40, height * 0.08);
            double leftRight = Math. max(30, menu.getWidth() * 0.08);
            return new Insets(topBottom, leftRight, topBottom, leftRight);
        }, menu.heightProperty(), menu.widthProperty()));

        // Adaptive spacing
        menu. spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(30, menu.getHeight() * 0.06),
                menu. heightProperty()
        ));

        // Header with beautiful title
        VBox headerBox = createHeader();

        // Decorative divider
        Region headerDivider = createDecorativeDivider();

        // Menu buttons container - NO EMOJIS
        VBox buttonsBox = new VBox();
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox. setFillWidth(true);
        buttonsBox.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(380, menu.getWidth() * 0.8),
                menu. widthProperty()
        ));

        buttonsBox.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math. max(12, menu.getHeight() * 0.018),
                menu. heightProperty()
        ));

        // Buttons WITHOUT emojis
        Button playButton = createMenuButton("Play", true);
        playButton.setOnAction(e -> switchToScreen(customizeScreen));

        Button rulesButton = createMenuButton("Rules", false);
        rulesButton.setOnAction(e -> switchToScreen(rulesScreen));

        Button aboutButton = createMenuButton("About", false);
        aboutButton. setOnAction(e -> switchToScreen(aboutScreen));

        Button exitButton = createMenuButton("Exit", false);
        exitButton. setOnAction(e -> {
            Platform.exit();
            System. exit(0);
        });

        buttonsBox.getChildren().addAll(playButton, rulesButton, aboutButton, exitButton);

        // Footer
        Label footer = new Label("— Strategy Game —");
        footer. setStyle("-fx-font-size: 11px; -fx-text-fill: #BEB0A7; -fx-letter-spacing: 3px;");

        // Spacers for vertical distribution
        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority. ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority. ALWAYS);

        menu.getChildren().addAll(topSpacer, headerBox, headerDivider, buttonsBox, bottomSpacer, footer);
        return menu;
    }

    /**
     * RULES SCREEN - Beautiful redesigned layout
     */
    private VBox createRulesScreen() {
        VBox rulesContent = new VBox(20);
        rulesContent.setAlignment(Pos.TOP_CENTER);
        rulesContent.setFillWidth(true);
        rulesContent.getStyleClass().add("welcome-content");

        rulesContent.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double width = rulesContent. getWidth();
            double leftRight = Math.max(25, width * 0.06);
            return new Insets(25, leftRight, 25, leftRight);
        }, rulesContent.widthProperty()));

        // Back button
        HBox topBar = new HBox();
        topBar. setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        // Beautiful header section
        VBox headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);

        Label title = new Label("How to Play");
        title.getStyleClass().add("screen-title");

        Label subtitle = new Label("Master the ancient art of five in a row");
        subtitle.getStyleClass().add("rules-subtitle");

        headerSection.getChildren().addAll(title, subtitle);

        // Rules cards
        VBox rulesBox = createBeautifulRulesSection();

        // Section title for examples
        VBox examplesHeader = new VBox(5);
        examplesHeader.setAlignment(Pos.CENTER);
        examplesHeader.setPadding(new Insets(15, 0, 5, 0));

        Label examplesTitle = new Label("WINNING PATTERNS");
        examplesTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #B0A098; -fx-letter-spacing: 3px;");

        Region miniDivider = new Region();
        miniDivider.setPrefHeight(1);
        miniDivider. setMaxWidth(60);
        miniDivider.setStyle("-fx-background-color: #D4C4BC;");

        examplesHeader. getChildren().addAll(examplesTitle, miniDivider);

        // Examples
        FlowPane examplesBox = createExamplesSection();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane. setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane. ScrollBarPolicy.AS_NEEDED);
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
     * Beautiful rules section with cards and icons
     */
    private VBox createBeautifulRulesSection() {
        VBox rulesBox = new VBox(12);
        rulesBox.setAlignment(Pos.CENTER);
        rulesBox.setMaxWidth(550);
        rulesBox.setPadding(new Insets(10, 0, 10, 0));

        // Rule cards with icons
        HBox rule1 = createRuleCard("◑", "Take Turns", "Place your pieces alternately on the intersections of the board");
        HBox rule2 = createRuleCard("❺", "Five to Win", "Connect exactly five pieces in an unbroken row to claim victory");
        HBox rule3 = createRuleCard("✛", "Any Direction", "Horizontal, vertical, and diagonal lines all count as valid wins");
        HBox rule4 = createRuleCard("♟", "Challenge AI", "Test your skills against an intelligent computer opponent");

        rulesBox.getChildren().addAll(rule1, rule2, rule3, rule4);
        return rulesBox;
    }

    /**
     * Creates a beautiful rule card
     */
    private HBox createRuleCard(String icon, String title, String description) {
        HBox card = new HBox(18);
        card. setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.getStyleClass().add("rule-card");

        // Icon container
        StackPane iconContainer = new StackPane();
        iconContainer. setMinWidth(50);
        iconContainer.setMinHeight(50);
        iconContainer.setMaxWidth(50);
        iconContainer.setMaxHeight(50);
        iconContainer. setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F5EDE8, #EBE0DA);" +
                        "-fx-background-radius: 12;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #C4A4A4;");
        iconContainer.getChildren(). add(iconLabel);

        // Text container
        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel. setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #8B7B75;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 300; -fx-text-fill: #A09088;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(400);

        textBox.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        card.getChildren().addAll(iconContainer, textBox);
        return card;
    }

    /**
     * CUSTOMIZE SCREEN - Piece customization with adaptive layout
     */
    private VBox createCustomizeScreen() {
        VBox customContent = new VBox();
        customContent. setAlignment(Pos.CENTER);
        customContent.setFillWidth(true);
        customContent. getStyleClass().add("welcome-content");

        customContent.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double width = customContent.getWidth();
            double height = customContent.getHeight();
            double topBottom = Math.max(30, height * 0.05);
            double leftRight = Math.max(30, width * 0.08);
            return new Insets(topBottom, leftRight, topBottom, leftRight);
        }, customContent.widthProperty(), customContent.heightProperty()));

        customContent.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math. max(25, customContent.getHeight() * 0.04),
                customContent.heightProperty()
        ));

        // Back button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        // Title
        Label title = new Label("Customize Your Pieces");
        title. getStyleClass().add("screen-title");

        // Customization section
        VBox customizationBox = createCustomizationSection();
        customizationBox.maxWidthProperty().bind(Bindings. createDoubleBinding(
                () -> Math.min(550, customContent.getWidth() * 0.9),
                customContent.widthProperty()
        ));

        // Play button
        Button playButton = new Button("START GAME");
        playButton.getStyleClass().add("primary-button");
        playButton.setOnAction(e -> {
            System.out.println("Begin button clicked!");
            System.out.println("Player: " + getPlayerShape() + " - " + getPlayerColor());
            System. out.println("AI: " + getAiShape() + " - " + getAiColor());
            if (onBegin != null) {
                onBegin.run();
            }
        });

        // Spacers
        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority. ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority. ALWAYS);

        customContent.getChildren().addAll(topBar, topSpacer, title, customizationBox, playButton, bottomSpacer);
        return customContent;
    }

    /**
     * ABOUT SCREEN - Information about the game
     */
    private VBox createAboutScreen() {
        VBox aboutContent = new VBox();
        aboutContent. setAlignment(Pos.CENTER);
        aboutContent.setFillWidth(true);
        aboutContent. getStyleClass().add("welcome-content");

        aboutContent. paddingProperty().bind(Bindings. createObjectBinding(() -> {
            double width = aboutContent.getWidth();
            double height = aboutContent. getHeight();
            double topBottom = Math.max(40, height * 0.06);
            double leftRight = Math.max(40, width * 0.1);
            return new Insets(topBottom, leftRight, topBottom, leftRight);
        }, aboutContent.widthProperty(), aboutContent.heightProperty()));

        aboutContent.spacingProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(20, aboutContent.getHeight() * 0.03),
                aboutContent.heightProperty()
        ));

        // Back button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backButton = createBackButton();
        backButton.setOnAction(e -> switchToScreen(mainMenuScreen));
        topBar.getChildren().add(backButton);

        // Title
        Label title = new Label("About Gomoku");
        title.getStyleClass().add("screen-title");

        // Description
        VBox description = new VBox(18);
        description. setAlignment(Pos.CENTER);
        description.maxWidthProperty().bind(Bindings. createDoubleBinding(
                () -> Math.min(500, aboutContent.getWidth() * 0.85),
                aboutContent.widthProperty()
        ));

        Label text1 = new Label("Gomoku (五目並べ) is an ancient Japanese strategy game,");
        text1.getStyleClass().add("about-text");
        text1.setWrapText(true);

        Label text2 = new Label("also known as Five in a Row or Gobang.");
        text2. getStyleClass().add("about-text");
        text2. setWrapText(true);

        Region divider = createDecorativeDivider();

        Label text3 = new Label("This implementation features an intelligent AI");
        text3. getStyleClass().add("about-text");
        text3. setWrapText(true);

        Label text4 = new Label("using the Minimax algorithm with alpha-beta pruning.");
        text4.getStyleClass(). add("about-text");
        text4.setWrapText(true);

        Region divider2 = createDecorativeDivider();

        Label version = new Label("Version 1.0");
        version.getStyleClass().add("about-version");

        Label developer = new Label("Developed with ♥");
        developer.getStyleClass().add("about-version");

        description.getChildren().addAll(text1, text2, divider, text3, text4, divider2, version, developer);

        // Spacers
        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        aboutContent.getChildren().addAll(topBar, topSpacer, title, description, bottomSpacer);
        return aboutContent;
    }

    /**
     * Switch between screens with fade animation
     */
    private void switchToScreen(VBox newScreen) {
        if (contentContainer. getChildren().isEmpty()) {
            contentContainer.getChildren(). add(newScreen);
            return;
        }

        VBox currentScreen = (VBox) contentContainer.getChildren().get(0);

        // Fade out current screen
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), currentScreen);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            contentContainer.getChildren(). clear();
            contentContainer.getChildren().add(newScreen);

            // Fade in new screen
            newScreen.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), newScreen);
            fadeIn. setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Creates menu button WITHOUT emoji
     */
    private Button createMenuButton(String text, boolean primary) {
        Button button = new Button(text);

        if (primary) {
            button.getStyleClass().add("primary-button");
        } else {
            button.getStyleClass().add("secondary-button");
        }

        button. setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /**
     * Creates a subtle back button
     */
    private Button createBackButton() {
        Button button = new Button("← Back");
        button.getStyleClass().add("back-button");
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
        return aiShapeCombo. getValue();
    }

    public String getAiColor() {
        return aiColorCombo.getValue();
    }

    // Helper methods
    private VBox createCustomizationSection() {
        VBox customBox = new VBox(20);
        customBox.setAlignment(Pos.CENTER);
        customBox.setFillWidth(true);

        VBox playerBox = createPlayerCustomization();
        VBox aiBox = createAiCustomization();

        customBox. getChildren().addAll(playerBox, aiBox);
        return customBox;
    }

    private VBox createPlayerCustomization() {
        VBox playerBox = new VBox(15);
        playerBox.setAlignment(Pos.CENTER);
        playerBox.setPadding(new Insets(20, 25, 20, 25));
        playerBox.getStyleClass().add("customize-card");

        Label playerLabel = new Label("Your Pieces");
        playerLabel. getStyleClass().add("customize-label");

        // Use FlowPane for adaptive layout
        FlowPane playerControls = new FlowPane();
        playerControls.setAlignment(Pos.CENTER);
        playerControls. setHgap(15);
        playerControls.setVgap(12);

        HBox shapeBox = new HBox(8);
        shapeBox.setAlignment(Pos. CENTER);
        Label shapeLabel = new Label("Shape:");
        shapeLabel. getStyleClass().add("customize-sublabel");
        playerShapeCombo = new ComboBox<>();
        playerShapeCombo.getItems().addAll(
                "Circle", "Flat Stone", "Hexagon", "Diamond",
                "Star", "Heart", "Flower", "Rounded Square"
        );
        playerShapeCombo.setValue("Circle");
        shapeBox.getChildren().addAll(shapeLabel, playerShapeCombo);

        HBox colorBox = new HBox(8);
        colorBox.setAlignment(Pos.CENTER);
        Label colorLabel = new Label("Color:");
        colorLabel.getStyleClass(). add("customize-sublabel");
        playerColorCombo = new ComboBox<>();
        playerColorCombo.getItems().addAll(
                "Obsidian Black", "Charcoal Gray", "Slate Blue", "Espresso Brown",
                "Ruby Red", "Sapphire Blue", "Emerald Green",
                "Amethyst Purple", "Topaz Orange", "Turquoise"
        );
        playerColorCombo. setValue("Obsidian Black");
        colorBox.getChildren().addAll(colorLabel, playerColorCombo);

        playerControls.getChildren(). addAll(shapeBox, colorBox);
        playerBox.getChildren().addAll(playerLabel, playerControls);

        return playerBox;
    }

    private VBox createAiCustomization() {
        VBox aiBox = new VBox(15);
        aiBox.setAlignment(Pos.CENTER);
        aiBox.setPadding(new Insets(20, 25, 20, 25));
        aiBox.getStyleClass().add("customize-card");

        Label aiLabel = new Label("AI Pieces");
        aiLabel.getStyleClass().add("customize-label");

        FlowPane aiControls = new FlowPane();
        aiControls.setAlignment(Pos.CENTER);
        aiControls.setHgap(15);
        aiControls.setVgap(12);

        HBox shapeBox = new HBox(8);
        shapeBox. setAlignment(Pos.CENTER);
        Label shapeLabel = new Label("Shape:");
        shapeLabel.getStyleClass(). add("customize-sublabel");
        aiShapeCombo = new ComboBox<>();
        aiShapeCombo.getItems().addAll(
                "Circle", "Flat Stone", "Hexagon", "Diamond",
                "Star", "Heart", "Flower", "Rounded Square"
        );
        aiShapeCombo. setValue("Circle");
        shapeBox. getChildren().addAll(shapeLabel, aiShapeCombo);

        HBox colorBox = new HBox(8);
        colorBox.setAlignment(Pos. CENTER);
        Label colorLabel = new Label("Color:");
        colorLabel.getStyleClass().add("customize-sublabel");
        aiColorCombo = new ComboBox<>();
        aiColorCombo.getItems().addAll(
                "Pearl White", "Ivory Cream", "Soft Beige", "Silver Gray",
                "Rose Pink", "Sky Blue", "Mint Green", "Lavender",
                "Peach", "Lemon Yellow", "Coral"
        );
        aiColorCombo. setValue("Pearl White");
        colorBox. getChildren().addAll(colorLabel, aiColorCombo);

        aiControls. getChildren().addAll(shapeBox, colorBox);
        aiBox.getChildren().addAll(aiLabel, aiControls);

        return aiBox;
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

        // Decorative pieces
        HBox piecesBox = new HBox(12);
        piecesBox.setAlignment(Pos.CENTER);

        Circle black = new Circle(10);
        black. getStyleClass().add("zen-piece-black");

        Circle white = new Circle(10);
        white. getStyleClass().add("zen-piece-white");

        piecesBox.getChildren().addAll(black, white);

        Label titleJp = new Label("五目並べ");
        titleJp.getStyleClass(). add("title-japanese");

        Label titleEn = new Label("GOMOKU");
        titleEn.getStyleClass().add("title-english");

        Label subtitle = new Label("Five in a Row");
        subtitle.getStyleClass().add("subtitle-zen");

        headerBox.getChildren().addAll(piecesBox, titleJp, titleEn, subtitle);
        return headerBox;
    }

    private FlowPane createExamplesSection() {
        FlowPane examplesBox = new FlowPane();
        examplesBox.setAlignment(Pos.CENTER);
        examplesBox.setHgap(15);
        examplesBox.setVgap(15);
        examplesBox.setPadding(new Insets(10, 0, 10, 0));

        VBox horizontal = createZenExample("Horizontal", ExampleType.HORIZONTAL);
        VBox vertical = createZenExample("Vertical", ExampleType. VERTICAL);
        VBox diagonal1 = createZenExample("Diagonal ↘", ExampleType. DIAGONAL_DOWN);
        VBox diagonal2 = createZenExample("Diagonal ↗", ExampleType. DIAGONAL_UP);

        examplesBox.getChildren(). addAll(horizontal, vertical, diagonal1, diagonal2);
        return examplesBox;
    }

    private enum ExampleType {
        HORIZONTAL, VERTICAL, DIAGONAL_DOWN, DIAGONAL_UP
    }

    private VBox createZenExample(String labelText, ExampleType type) {
        VBox container = new VBox(10);
        container. setAlignment(Pos.CENTER);
        container.setPadding(new Insets(14, 16, 14, 16));
        container.getStyleClass().add("example-card");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2);
        grid. setVgap(2);
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
                    Circle piece = new Circle(8);
                    piece.getStyleClass(). add("zen-example-piece");
                    cell.getChildren().add(piece);
                }

                grid. add(cell, c, r);
            }
        }

        Label label = new Label(labelText);
        label. getStyleClass().add("zen-example-label");

        container.getChildren().addAll(grid, label);
        return container;
    }
}