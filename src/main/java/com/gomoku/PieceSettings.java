package com.gomoku;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;

/**
 * PieceSettings - Stores customization settings for game pieces
 */
public class PieceSettings {

    public enum PieceShape {
        CIRCLE, SQUARE, DIAMOND, STAR
    }

    private String playerShapeName;
    private String playerColorName;
    private String aiShapeName;
    private String aiColorName;

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

    private PieceShape parseShape(String shapeName) {
        switch (shapeName) {
            case "Square": return PieceShape.SQUARE;
            case "Diamond": return PieceShape.DIAMOND;
            case "Star": return PieceShape.STAR;
            default: return PieceShape.CIRCLE;
        }
    }

    private Color parseColor(String colorName) {
        switch (colorName) {
            // Dark colors (for player)
            case "Black": return Color.rgb(45, 45, 45);
            case "Dark Brown": return Color.rgb(101, 67, 33);
            case "Navy Blue": return Color.rgb(0, 31, 63);
            case "Dark Green": return Color.rgb(19, 79, 52);
            case "Purple": return Color.rgb(88, 24, 69);
            case "Crimson": return Color.rgb(153, 0, 0);

            // Light colors (for AI)
            case "White": return Color.rgb(255, 255, 255);
            case "Cream": return Color.rgb(255, 253, 208);
            case "Light Blue": return Color.rgb(173, 216, 230);
            case "Mint Green": return Color.rgb(152, 251, 152);
            case "Pink": return Color.rgb(255, 182, 193);
            case "Light Yellow": return Color.rgb(255, 255, 224);

            default: return Color.BLACK;
        }
    }

    /**
     * Creates a shape based on the piece settings
     */
    public static Shape createShape(PieceShape shape, double size) {
        switch (shape) {
            case CIRCLE:
                return new Circle(size);

            case SQUARE:
                Rectangle square = new Rectangle(size * 2, size * 2);
                square.setArcWidth(4);
                square.setArcHeight(4);
                return square;

            case DIAMOND:
                Polygon diamond = new Polygon();
                diamond.getPoints().addAll(
                        0.0, -size,      // Top
                        size, 0.0,       // Right
                        0.0, size,       // Bottom
                        -size, 0.0       // Left
                );
                return diamond;

            case STAR:
                Polygon star = new Polygon();
                double outerRadius = size;
                double innerRadius = size * 0.4;
                for (int i = 0; i < 10; i++) {
                    double radius = (i % 2 == 0) ? outerRadius : innerRadius;
                    double angle = Math.PI / 2 - (i * Math.PI / 5);
                    star.getPoints().addAll(
                            radius * Math.cos(angle),
                            -radius * Math.sin(angle)
                    );
                }
                return star;

            default:
                return new Circle(size);
        }
    }
}