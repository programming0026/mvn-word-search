package com.juniverse.word.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.juniverse.word.dto.WordDto;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Pruebas de la lógica de la sopa de letras, sin interfaz gráfica. */
class WordSearchModelTest {

    private static List<WordDto> words(String... texts) {
        List<WordDto> list = new ArrayList<>();
        for (String text : texts) {
            list.add(new WordDto(text));
        }
        return list;
    }

    @Test
    void elParserNormalizaIgnoraComentariosYLineasVacias() {
        List<String> parsed = WordListParser.parse(List.of("  java ", "", "# comentario", "spring boot", "Swing"));
        assertEquals(List.of("JAVA", "SPRINGBOOT", "SWING"), parsed);
    }

    @Test
    void generaLaSopaConTodasLasPalabrasEnSuSitio() {
        WordSearchGrid grid = new WordSearchGrid(15, new Random(7));
        List<WordDto> list = words("JAVA", "SWING", "PROGRAMA", "CODIGO", "CLASE");
        List<WordDto> notPlaced = grid.generate(list);
        assertTrue(notPlaced.isEmpty());
        for (WordDto word : list) {
            assertNotNull(word.positions);
            assertEquals(word.text.length(), word.positions.size());
            assertEquals(word.text, grid.lettersAt(word.positions));
        }
    }

    @Test
    void todasLasCeldasQuedanConUnaLetraMayuscula() {
        WordSearchGrid grid = new WordSearchGrid(10, new Random(1));
        grid.generate(words("HOLA"));
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                char ch = grid.get(r, c);
                assertTrue(ch >= 'A' && ch <= 'Z', "celda " + r + "," + c + " = " + ch);
            }
        }
    }

    @Test
    void unaPalabraMasLargaQueLaCuadriculaSeDescarta() {
        WordSearchGrid grid = new WordSearchGrid(5, new Random(3));
        List<WordDto> notPlaced = grid.generate(words("ABC", "PALABRAMUYLARGA"));
        assertEquals(1, notPlaced.size());
        assertEquals("PALABRAMUYLARGA", notPlaced.get(0).text);
        assertNull(notPlaced.get(0).positions);
    }

    @Test
    void canPlaceRespetaLosBordesYLasLetrasExistentes() {
        WordSearchGrid grid = new WordSearchGrid(5, new Random(0));
        assertTrue(grid.canPlace("ABC", 0, 0, 6)); // hacia abajo
        assertFalse(grid.canPlace("ABC", 4, 4, 6)); // se sale por abajo
        WordDto first = new WordDto("ABC");
        grid.place(first, 0, 0, 6);
        assertTrue(grid.canPlace("AXY", 0, 0, 4)); // comparte la A
        assertFalse(grid.canPlace("ZZZ", 0, 0, 4)); // choca con la A
    }

    @Test
    void laSeleccionDebeEstarEnLineaRecta() {
        assertTrue(LineSelection.isStraightLine(new Point(2, 2), new Point(2, 7)));
        assertTrue(LineSelection.isStraightLine(new Point(2, 2), new Point(6, 2)));
        assertTrue(LineSelection.isStraightLine(new Point(2, 2), new Point(5, 5)));
        assertFalse(LineSelection.isStraightLine(new Point(2, 2), new Point(3, 5)));
    }

    @Test
    void lasCeldasEntreDosPuntosIncluyenLosExtremos() {
        List<Point> points = LineSelection.pointsBetween(new Point(1, 1), new Point(4, 4));
        assertEquals(List.of(new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4)), points);
        List<Point> back = LineSelection.pointsBetween(new Point(0, 3), new Point(0, 0));
        assertEquals(List.of(new Point(0, 3), new Point(0, 2), new Point(0, 1), new Point(0, 0)), back);
    }

    @Test
    void encuentraLaPalabraEnCualquierSentidoSoloSiEstaPendiente() {
        List<WordDto> list = words("JAVA", "SWING");
        assertEquals("JAVA", LineSelection.findPending(list, Set.of(), "JAVA").text);
        assertEquals("SWING", LineSelection.findPending(list, Set.of(), "GNIWS").text);
        assertNull(LineSelection.findPending(list, Set.of("JAVA"), "JAVA"));
        assertNull(LineSelection.findPending(list, Set.of(), "OTRA"));
    }
}
