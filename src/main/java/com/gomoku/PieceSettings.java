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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.ArcTo;

/**
 * PieceSettings - Beautiful mix of classic, bright, and cute shapes!
 */
public class PieceSettings {

    public enum PieceShape {
        CIRCLE, FLAT_STONE, HEXAGON, DIAMOND, STAR, HEART, FLOWER, ROUNDED_SQUARE
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
            case "Flat Stone": return PieceShape.FLAT_STONE;
            case "Hexagon": return PieceShape.HEXAGON;
            case "Diamond": return PieceShape.DIAMOND;
            case "Star": return PieceShape.STAR;
            case "Heart": return PieceShape.HEART;
            case "Flower": return PieceShape.FLOWER;
            case "Rounded Square": return PieceShape.ROUNDED_SQUARE;
            default: return PieceShape.CIRCLE;
        }
    }

    private Color parseColor(String colorName) {
        switch (colorName) {
            // === КЛАССИЧЕСКИЕ ТЁМНЫЕ ===
            case "Obsidian Black": return Color.rgb(26, 26, 26);
            case "Charcoal Gray": return Color.rgb(54, 54, 54);
            case "Slate Blue": return Color.rgb(61, 79, 92);
            case "Espresso Brown": return Color.rgb(62, 39, 35);

            // === ЯРКИЕ ТЁМНЫЕ ===
            case "Ruby Red": return Color.rgb(204, 0, 51);
            case "Sapphire Blue": return Color.rgb(15, 82, 186);
            case "Emerald Green": return Color.rgb(0, 128, 96);
            case "Amethyst Purple": return Color.rgb(128, 0, 128);
            case "Topaz Orange": return Color.rgb(204, 85, 0);
            case "Turquoise": return Color.rgb(0, 128, 128);

            // === КЛАССИЧЕСКИЕ СВЕТЛЫЕ ===
            case "Pearl White": return Color.rgb(245, 245, 245);
            case "Ivory Cream": return Color.rgb(255, 248, 231);
            case "Soft Beige": return Color.rgb(245, 230, 211);
            case "Silver Gray": return Color.rgb(192, 192, 192);

            // === ЯРКИЕ СВЕТЛЫЕ (ПАСТЕЛЬНЫЕ) ===
            case "Rose Pink": return Color.rgb(255, 182, 193);
            case "Sky Blue": return Color.rgb(135, 206, 250);
            case "Mint Green": return Color.rgb(152, 255, 152);
            case "Lavender": return Color.rgb(230, 204, 255);
            case "Peach": return Color.rgb(255, 218, 185);
            case "Lemon Yellow": return Color.rgb(255, 255, 153);
            case "Coral": return Color.rgb(255, 127, 80);

            // Fallback
            default: return Color.rgb(26, 26, 26);
        }
    }

    /**
     * Creates beautiful shapes including classic and cute ones!
     */
    public static Shape createShape(PieceShape shape, double size) {
        switch (shape) {
            case CIRCLE:
                // Классический круг
                return new Circle(size);

            case FLAT_STONE:
                // Реалистичный камень Го
                Ellipse stone = new Ellipse(size, size * 0.8);
                return stone;

            case HEXAGON:
                // Современный шестиугольник
                Polygon hexagon = new Polygon();
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 3 * i - Math.PI / 6;
                    hexagon.getPoints().addAll(
                            size * Math.cos(angle),
                            size * Math.sin(angle)
                    );
                }
                return hexagon;

            case DIAMOND:
                // Классический ромб
                Polygon diamond = new Polygon();
                diamond.getPoints().addAll(
                        0.0, -size,      // Top
                        size, 0.0,       // Right
                        0.0, size,       // Bottom
                        -size, 0.0       // Left
                );
                return diamond;

            case STAR:
                // Пятиконечная звезда (острые лучи)
                Polygon star = new Polygon();
                double outerRadius = size;
                double innerRadius = size * 0.38; // Более тонкие лучи
                for (int i = 0; i < 10; i++) {
                    double radius = (i % 2 == 0) ? outerRadius : innerRadius;
                    double angle = Math.PI / 2 - (i * Math.PI / 5);
                    star.getPoints().addAll(
                            radius * Math.cos(angle),
                            -radius * Math.sin(angle)
                    );
                }
                return star;

            case HEART:
                // Красивое сердечко с плавными изгибами ❤
                javafx.scene.shape.Path heart = new javafx. scene.shape.Path();

                double s = size * 0.85;

                // Начинаем с нижней точки
                heart. getElements().add(new javafx.scene.shape.MoveTo(0, s * 0.7));

                // Левая сторона сердца (кривая Безье)
                heart. getElements().add(new javafx.scene.shape.CubicCurveTo(
                        -s * 0.1, s * 0.5,    // control point 1
                        -s * 0.9, s * 0.2,    // control point 2
                        -s * 0.9, -s * 0.2    // end point
                ));

                // Левая верхняя доля
                heart.getElements().add(new javafx.scene.shape.CubicCurveTo(
                        -s * 0.9, -s * 0.6,   // control point 1
                        -s * 0.5, -s * 0.9,   // control point 2
                        0, -s * 0.5           // end point (верхняя впадина)
                ));

                // Правая верхняя доля
                heart.getElements().add(new javafx.scene. shape.CubicCurveTo(
                        s * 0.5, -s * 0.9,    // control point 1
                        s * 0.9, -s * 0.6,    // control point 2
                        s * 0.9, -s * 0.2     // end point
                ));

                // Правая сторона сердца (обратно к нижней точке)
                heart.getElements().add(new javafx.scene. shape.CubicCurveTo(
                        s * 0.9, s * 0.2,     // control point 1
                        s * 0.1, s * 0.5,     // control point 2
                        0, s * 0.7            // end point (нижняя точка)
                ));

                heart.getElements().add(new javafx.scene. shape. ClosePath());

                return heart;

            case FLOWER:
                // Простой и красивый цветочек 🌸
                Polygon flower = new Polygon();

                // Параметры
                int petals = 6; // 6 лепестков для симметрии
                double innerR = size * 0.35; // Радиус "впадин" между лепестками
                double outerR = size * 1.0;  // Радиус кончиков лепестков

                // Создаём волнистую форму
                int pointsPerPetal = 8;
                int totalPoints = petals * pointsPerPetal;

                for (int i = 0; i < totalPoints; i++) {
                    double angle = (i * 2 * Math.PI / totalPoints) - Math.PI / 2;

                    // Плавный переход между innerR и outerR
                    double petalPhase = (i % pointsPerPetal) / (double) pointsPerPetal;
                    // Используем sin для плавной кривой лепестка
                    double radiusFactor = Math.sin(petalPhase * Math.PI);
                    double radius = innerR + (outerR - innerR) * radiusFactor;

                    flower.getPoints().addAll(
                            radius * Math.cos(angle),
                            radius * Math.sin(angle)
                    );
                }

                return flower;

            case ROUNDED_SQUARE:
                // Мягкий закруглённый квадрат
                Rectangle roundedSquare = new Rectangle(size * 1.8, size * 1.8);
                roundedSquare.setArcWidth(size * 0.6);
                roundedSquare.setArcHeight(size * 0.6);
                roundedSquare.setX(-size * 0.9);
                roundedSquare.setY(-size * 0.9);
                return roundedSquare;

            default:
                return new Circle(size);
        }
    }
}