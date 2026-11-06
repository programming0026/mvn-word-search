
package com.juniverse.word;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.juniverse.word.gui.WordsFrame;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada del juego Sopa de Letras.
 *
 * @author Java Universe
 */
public class Main extends JFrame {

    /**
     * Abre el juego con el tema oscuro FlatLaf.
     * @param args no se usan
     */
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(WordsFrame::new);
    }

   
}
