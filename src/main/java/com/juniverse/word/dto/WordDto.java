package com.juniverse.word.dto;

import java.awt.Point;
import java.util.List;
import javax.swing.JLabel;

/**
 * Palabra de la sopa de letras: su texto, las celdas que ocupa y su etiqueta en el panel de palabras.
 *
 * @author Java Universe
 */
public class WordDto {

    public String text;
    public List<Point> positions;
    public JLabel label;

    /**
     * Crea la palabra.
     * @param texto palabra en mayúsculas
     */
    public WordDto(String text) {
        this.text = text;
    }
}
