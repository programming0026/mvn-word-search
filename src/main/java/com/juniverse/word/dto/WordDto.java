/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.juniverse.word.dto;

import java.awt.Point;
import java.util.List;
import javax.swing.JLabel;

/**
 * Palabra de la sopa de letras: su texto, las celdas que ocupa y su etiqueta en el panel de palabras.
 *
 * @author ajuar
 */
public class WordDto {

    public String texto;
    public List<Point> posiciones;
    public JLabel label;

    /**
     * Crea la palabra.
     * @param texto palabra en mayúsculas
     */
    public WordDto(String texto) {
        this.texto = texto;
    }
}
