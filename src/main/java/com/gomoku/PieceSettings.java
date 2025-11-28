package com.gomoku;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.CubicCurveTo;

/**
 * Manages the visual appearance of game pieces.
 * <p>
 * This class handles the configuration for player and AI pieces, including
 * their shapes (Geometry) and colors. It provides a factory method to generate
 * JavaFX Shape objects based on these settings.
 */
public class PieceSettings {

    /**
     * Enumeration of supported piece shapes.
     */
    public enum PieceShape {
        CIRCLE, FLAT_STONE, HEXAGON, DIAMOND, STAR, HEART, FLOWER, ROUNDED_SQUARE
    }

    private String playerShapeName;
    private String playerColorName;
    private String aiShapeName;
    private String aiColorName;

    /**
     * Constructs a new PieceSettings configuration.
     *
     * @param playerShape Name of the shape for the human player.
     * @param playerColor Name of the color for the human player.
     * @param aiShape     Name of the shape for the AI.
     * @param aiColor     Name of the color for the AI.
     */
    public PieceSettings(String playerShape, String playerColor, String aiShape, String aiColor) {
        this.playerShapeName = playerShape;
        this.playerColorName = playerColor;
        this.aiShapeName = aiShape;
        this.aiColorName = aiColor;
    }

    public PieceShape getPlayerShape() {
        return parseShape(playerShapeName);
    }

    public PieceShape getAiShape() {
        return parseShape(aiShapeName);
    }

    public Color getPlayerColor() {
        return parseColor(playerColorName);
    }

    public Color getAiColor() {
        return parseColor(aiColorName);
    }

    /**
     * Converts a string representation of a shape into the corresponding Enum.
     */
    private PieceShape parseShape(String shapeName) {
        if (shapeName == null) return PieceShape.CIRCLE;

        return switch (shapeName) {
            case "Flat Stone" -> PieceShape.FLAT_STONE;
            case "Hexagon" -> PieceShape.HEXAGON;
            case "Diamond" -> PieceShape.DIAMOND;
            case "Star" -> PieceShape.STAR;
            case "Heart" -> PieceShape.HEART;
            case "Flower" -> PieceShape.FLOWER;
            case "Rounded Square" -> PieceShape.ROUNDED_SQUARE;
            default -> PieceShape.CIRCLE;
        };
    }

    /**
     * Converts a color name into a JavaFX Color object.
     * Includes a wide palette of Classic, Vivid, and Pastel tones.
     */
    private Color parseColor(String colorName) {
        if (colorName == null) return Color.BLACK;

        return switch (colorName) {
            // === Classic Dark ===
            case "Obsidian Black" -> Color.rgb(26, 26, 26);
            case "Charcoal Gray" -> Color.rgb(54, 54, 54);
            case "Slate Blue" -> Color.rgb(61, 79, 92);
            case "Espresso Brown" -> Color.rgb(62, 39, 35);

            // === Vivid Dark ===
            case "Ruby Red" -> Color.rgb(204, 0, 51);
            case "Sapphire Blue" -> Color.rgb(15, 82, 186);
            case "Emerald Green" -> Color.rgb(0, 128, 96);
            case "Amethyst Purple" -> Color.rgb(128, 0, 128);
            case "Topaz Orange" -> Color.rgb(204, 85, 0);
            case "Turquoise" -> Color.rgb(0, 128, 128);

            // === Classic Light ===
            case "Pearl White" -> Color.rgb(245, 245, 245);
            case "Ivory Cream" -> Color.rgb(255, 248, 231);
            case "Soft Beige" -> Color.rgb(245, 230, 211);
            case "Silver Gray" -> Color.rgb(192, 192, 192);

            // === Pastel Light ===
            case "Rose Pink" -> Color.rgb(255, 182, 193);
            case "Sky Blue" -> Color.rgb(135, 206, 250);
            case "Mint Green" -> Color.rgb(152, 255, 152);
            case "Lavender" -> Color.rgb(230, 204, 255);
            case "Peach" -> Color.rgb(255, 218, 185);
            case "Lemon Yellow" -> Color.rgb(255, 255, 153);
            case "Coral" -> Color.rgb(255, 127, 80);

            // Fallback
            default -> Color.rgb(26, 26, 26);
        };
    }

    /**
     * Factory method to create a JavaFX Shape based on the selected PieceShape enum.
     *
     * @param shape The desired shape type.
     * @param size  The radius or half-width of the shape.
     * @return A constructed JavaFX Shape object.
     */
    public static Shape createShape(PieceShape shape, double size) {
        return switch (shape) {
            case CIRCLE -> new Circle(size);

            case FLAT_STONE -> new Ellipse(size, size * 0.8);

            case HEXAGON -> {
                Polygon hexagon = new Polygon();
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 3 * i - Math.PI / 6;
                    hexagon.getPoints().addAll(
                            size * Math.cos(angle),
                            size * Math.sin(angle)
                    );
                }
                yield hexagon;
            }

            case DIAMOND -> {
                Polygon diamond = new Polygon();
                double topWidth = size * 0.5;
                double midWidth = size * 1.0;
                double topY = -size * 0.5;
                double midY = -size * 0.15;
                double bottomY = size * 0.9;

                diamond.getPoints().addAll(
                        -topWidth, topY,
                        topWidth, topY,
                        midWidth, midY,
                        0.0, bottomY,
                        -midWidth, midY
                );
                yield diamond;
            }

            case STAR -> {
                Polygon star = new Polygon();
                double outerRadius = size;
                double innerRadius = size * 0.38;
                for (int i = 0; i < 10; i++) {
                    double radius = (i % 2 == 0) ? outerRadius : innerRadius;
                    double angle = Math.PI / 2 - (i * Math.PI / 5);
                    star.getPoints().addAll(
                            radius * Math.cos(angle),
                            -radius * Math.sin(angle)
                    );
                }
                yield star;
            }

            case HEART -> {
                Path heart = new Path();
                double s = size * 0.85;

                heart.getElements().add(new MoveTo(0, s * 0.7));

                heart.getElements().add(new CubicCurveTo(
                        -s * 0.1, s * 0.5,
                        -s * 0.9, s * 0.2,
                        -s * 0.9, -s * 0.2
                ));

                heart.getElements().add(new CubicCurveTo(
                        -s * 0.9, -s * 0.6,
                        -s * 0.5, -s * 0.9,
                        0, -s * 0.5
                ));

                heart.getElements().add(new CubicCurveTo(
                        s * 0.5, -s * 0.9,
                        s * 0.9, -s * 0.6,
                        s * 0.9, -s * 0.2
                ));

                heart.getElements().add(new CubicCurveTo(
                        s * 0.9, s * 0.2,
                        s * 0.1, s * 0.5,
                        0, s * 0.7
                ));

                heart.getElements().add(new javafx.scene.shape.ClosePath());
                yield heart;
            }

            case FLOWER -> {
                Polygon flower = new Polygon();
                int petals = 6;
                double innerR = size * 0.35;
                double outerR = size * 1.0;
                int pointsPerPetal = 8;
                int totalPoints = petals * pointsPerPetal;

                for (int i = 0; i < totalPoints; i++) {
                    double angle = (i * 2 * Math.PI / totalPoints) - Math.PI / 2;
                    double petalPhase = (i % pointsPerPetal) / (double) pointsPerPetal;
                    double radiusFactor = Math.sin(petalPhase * Math.PI);
                    double radius = innerR + (outerR - innerR) * radiusFactor;

                    flower.getPoints().addAll(
                            radius * Math.cos(angle),
                            radius * Math.sin(angle)
                    );
                }
                yield flower;
            }

            case ROUNDED_SQUARE -> {
                Rectangle roundedSquare = new Rectangle(size * 1.8, size * 1.8);
                roundedSquare.setArcWidth(size * 0.6);
                roundedSquare.setArcHeight(size * 0.6);
                roundedSquare.setX(-size * 0.9);
                roundedSquare.setY(-size * 0.9);
                yield roundedSquare;
            }
        };
    }
}