package com.juniverse.word.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Interpreta una lista de palabras (una por línea): quita espacios, pasa a mayúsculas e ignora las líneas vacías y
 * los comentarios que empiezan con {@code #}.
 */
public final class WordListParser {

    private WordListParser() {
    }

    /**
     * Normaliza las líneas de un archivo de palabras.
     *
     * @param lines líneas del archivo
     * @return palabras válidas en mayúsculas y sin espacios (con espacios se confundirían con celdas vacías)
     */
    public static List<String> parse(Iterable<String> lines) {
        List<String> words = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.trim().toUpperCase().replaceAll("\\s+", "");
            if (!line.isEmpty() && !line.startsWith("#")) {
                words.add(line);
            }
        }
        return words;
    }
}
