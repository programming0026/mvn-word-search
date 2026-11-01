package com.juniverse.word.model;

import com.juniverse.word.dto.WordDto;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reglas de la selección con el ratón: celdas en línea recta y comparación del texto elegido con las palabras
 * pendientes. No depende de la interfaz gráfica.
 */
public final class LineSelection {

    private LineSelection() {
    }

    /** Indica si dos celdas están en línea recta (horizontal, vertical o diagonal). */
    public static boolean isStraightLine(Point start, Point end) {
        int rowDiff = Math.abs(end.x - start.x);
        int colDiff = Math.abs(end.y - start.y);
        return rowDiff == 0 || colDiff == 0 || rowDiff == colDiff;
    }

    /** Celdas en línea recta entre dos puntos, ambos incluidos. */
    public static List<Point> pointsBetween(Point start, Point end) {
        List<Point> points = new ArrayList<>();
        int rows = end.x - start.x;
        int columns = end.y - start.y;
        int steps = Math.max(Math.abs(rows), Math.abs(columns));
        int stepRow = rows == 0 ? 0 : rows / Math.abs(rows);
        int stepCol = columns == 0 ? 0 : columns / Math.abs(columns);
        for (int i = 0; i <= steps; i++) {
            points.add(new Point(start.x + stepRow * i, start.y + stepCol * i));
        }
        return points;
    }

    /**
     * Busca la palabra de la lista que coincide con el texto seleccionado (en cualquier sentido) y aún no se ha
     * encontrado.
     *
     * @param words palabras del juego
     * @param found textos de las palabras ya encontradas
     * @param selected letras seleccionadas
     * @return la palabra, o {@code null} si no coincide con ninguna pendiente
     */
    public static WordDto findPending(List<WordDto> words, Set<String> found, String selected) {
        String reversed = new StringBuilder(selected).reverse().toString();
        for (WordDto w : words) {
            if (!found.contains(w.text) && (w.text.equals(selected) || w.text.equals(reversed))) {
                return w;
            }
        }
        return null;
    }
}
