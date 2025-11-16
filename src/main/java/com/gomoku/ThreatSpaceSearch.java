package com.gomoku;

import java.util.ArrayList;
import java.util.List;

/**
 * НОВЫЙ КЛАСС - создайте новый файл src/main/java/com/gomoku/ThreatSpaceSearch.java
 *
 * Этот класс находит только "угрожающие" ходы, сильно сокращая branching factor.
 * Вместо проверки всех ~50 возможных ходов, ищет только ~5-10 критических.
 */
public class ThreatSpaceSearch {
    private final Board board;
    private final GameLogic logic;
    private final int boardSize;

    public ThreatSpaceSearch(Board board, GameLogic logic, int boardSize) {
        this.board = board;
        this.logic = logic;
        this.boardSize = boardSize;
    }

    /**
     * Находит критические ходы для игрока
     * @param player ID игрока
     * @param opponent ID противника
     * @return список приоритетных ходов
     */
    public List<Move> findThreats(int player, int opponent) {
        List<Move> threats = new ArrayList<>();

        // 1. КРИТИЧНО: Проверяем немедленный выигрыш
        List<Move> winningMoves = findWinningMoves(player);
        if (!winningMoves.isEmpty()) {
            return winningMoves; // Возвращаем только выигрышный ход
        }

        // 2. КРИТИЧНО: Блокируем немедленный выигрыш противника
        List<Move> blockingMoves = findWinningMoves(opponent);
        if (!blockingMoves.isEmpty()) {
            threats.addAll(blockingMoves);
            return threats; // Нужно обязательно блокировать
        }

        // 3. Ищем форсирующие ходы (открытые четвёрки/тройки)
        threats.addAll(findOpenFours(player));
        threats.addAll(findOpenThrees(player));

        // 4. Блокируем угрозы противника
        threats.addAll(findOpenFours(opponent));
        threats.addAll(findOpenThrees(opponent));

        return threats;
    }

    /**
     * Находит ходы, которые дают немедленную победу
     */
    private List<Move> findWinningMoves(int player) {
        List<Move> moves = new ArrayList<>();

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    // Временно делаем ход
                    board.setCell(r, c, player);

                    if (logic.checkWin(r, c, player)) {
                        moves.add(new Move(r, c));
                    }

                    // Отменяем ход
                    board.setCell(r, c, Board.EMPTY);
                }
            }
        }

        return moves;
    }

    /**
     * Находит ходы, создающие открытую четвёрку (_XXXX_)
     */
    private List<Move> findOpenFours(int player) {
        List<Move> moves = new ArrayList<>();

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    board.setCell(r, c, player);

                    // Проверяем, создаёт ли этот ход четвёрку с открытыми концами
                    if (hasOpenFour(r, c, player)) {
                        moves.add(new Move(r, c));
                    }

                    board.setCell(r, c, Board.EMPTY);
                }
            }
        }

        return moves;
    }

    /**
     * Находит ходы, создающие открытую тройку (_XXX_)
     */
    private List<Move> findOpenThrees(int player) {
        List<Move> moves = new ArrayList<>();

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                if (board.getCell(r, c) == Board.EMPTY) {
                    board.setCell(r, c, player);

                    if (hasOpenThree(r, c, player)) {
                        moves.add(new Move(r, c));
                    }

                    board.setCell(r, c, Board.EMPTY);
                }
            }
        }

        return moves;
    }

    /**
     * Проверяет, есть ли открытая четвёрка в позиции
     */
    private boolean hasOpenFour(int r, int c, int player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1;
            int leftOpen = 0;
            int rightOpen = 0;

            // Считаем вправо
            for (int i = 1; i <= 4; i++) {
                int nr = r + dir[0] * i;
                int nc = c + dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    rightOpen = 1;
                    break;
                } else {
                    break;
                }
            }

            // Считаем влево
            for (int i = 1; i <= 4; i++) {
                int nr = r - dir[0] * i;
                int nc = c - dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    leftOpen = 1;
                    break;
                } else {
                    break;
                }
            }

            // Открытая четвёрка: 4 фишки и оба конца открыты
            if (count == 4 && leftOpen == 1 && rightOpen == 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяет, есть ли открытая тройка в позиции
     */
    private boolean hasOpenThree(int r, int c, int player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1;
            int leftOpen = 0;
            int rightOpen = 0;

            // Считаем вправо
            for (int i = 1; i <= 3; i++) {
                int nr = r + dir[0] * i;
                int nc = c + dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    rightOpen = 1;
                    break;
                } else {
                    break;
                }
            }

            // Считаем влево
            for (int i = 1; i <= 3; i++) {
                int nr = r - dir[0] * i;
                int nc = c - dir[1] * i;

                if (!board.isValid(nr, nc)) break;

                if (board.getCell(nr, nc) == player) {
                    count++;
                } else if (board.getCell(nr, nc) == Board.EMPTY) {
                    leftOpen = 1;
                    break;
                } else {
                    break;
                }
            }

            // Открытая тройка: 3 фишки и оба конца открыты
            if (count == 3 && leftOpen == 1 && rightOpen == 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Вспомогательный класс для представления хода
     */
    public static class Move {
        public final int r, c;

        public Move(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return r == move.r && c == move.c;
        }

        @Override
        public int hashCode() {
            return 31 * r + c;
        }
    }
}
