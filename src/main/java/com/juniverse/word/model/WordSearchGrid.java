package com.juniverse.word.model;

import com.juniverse.word.dto.WordDto;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cuadrícula cuadrada de la sopa de letras: coloca las palabras en las ocho direcciones y rellena el resto con letras
 * al azar. No depende de la interfaz gráfica.
 */
public class WordSearchGrid {

    private static final int[] ROW_STEP = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] COL_STEP = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int MAX_ATTEMPTS = 1000;
    private static final char EMPTY = ' ';

    private final int size;
    private final Random random;
    private final char[][] cells;

    /**
     * Crea una cuadrícula vacía.
     *
     * @param size lado de la cuadrícula
     * @param random generador de números aleatorios (se inyecta para probar con una semilla fija)
     */
    public WordSearchGrid(int size, Random random) {
        this.size = size;
        this.random = random;
        this.cells = new char[size][size];
        clear();
    }

    /** Lado de la cuadrícula. */
    public int size() {
        return size;
    }

    /** Letra de una celda. */
    public char get(int row, int col) {
        return cells[row][col];
    }

    /** Letras de una lista de celdas, en orden. */
    public String lettersAt(List<Point> points) {
        StringBuilder sb = new StringBuilder();
        for (Point p : points) {
            sb.append(cells[p.x][p.y]);
        }
        return sb.toString();
    }

    /**
     * Genera una sopa nueva: coloca cada palabra en un lugar y dirección aleatorios (hasta 1000 intentos) y rellena el
     * resto con letras al azar. Guarda en cada palabra las celdas que ocupa.
     *
     * @param words palabras que se colocan
     * @return las palabras que no cupieron (su {@code positions} queda en {@code null})
     */
    public List<WordDto> generate(List<WordDto> words) {
        clear();
        for (WordDto word : words) {
            word.positions = null; // posiciones de una partida anterior
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < MAX_ATTEMPTS) {
                int row = random.nextInt(size);
                int col = random.nextInt(size);
                int direction = random.nextInt(8);
                if (canPlace(word.text, row, col, direction)) {
                    place(word, row, col, direction);
                    placed = true;
                }
                attempts++;
            }
        }
        List<WordDto> notPlaced = words.stream().filter(w -> w.positions == null).toList();
        fillWithRandomLetters();
        return notPlaced;
    }

    /**
     * Comprueba si la palabra cabe desde una posición y dirección, sin chocar con letras distintas.
     *
     * @param word palabra
     * @param row fila inicial
     * @param column columna inicial
     * @param direction dirección (0-7, las ocho vecinas)
     */
    public boolean canPlace(String word, int row, int column, int direction) {
        int length = word.length();
        int finalRow = row + ROW_STEP[direction] * (length - 1);
        int finalColumn = column + COL_STEP[direction] * (length - 1);
        if (finalRow < 0 || finalRow >= size || finalColumn < 0 || finalColumn >= size) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            int f = row + ROW_STEP[direction] * i;
            int c = column + COL_STEP[direction] * i;
            if (cells[f][c] != EMPTY && cells[f][c] != word.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Escribe la palabra en la cuadrícula y guarda las celdas que ocupa.
     *
     * @param word palabra
     * @param row fila inicial
     * @param column columna inicial
     * @param direction dirección (0-7)
     */
    public void place(WordDto word, int row, int column, int direction) {
        word.positions = new ArrayList<>();
        for (int i = 0; i < word.text.length(); i++) {
            int f = row + ROW_STEP[direction] * i;
            int c = column + COL_STEP[direction] * i;
            cells[f][c] = word.text.charAt(i);
            word.positions.add(new Point(f, c));
        }
    }

    private void clear() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = EMPTY;
            }
        }
    }

    private void fillWithRandomLetters() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j] == EMPTY) {
                    cells[i][j] = (char) ('A' + random.nextInt(26));
                }
            }
        }
    }
}
