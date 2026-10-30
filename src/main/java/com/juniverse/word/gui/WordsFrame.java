/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.juniverse.word.gui;

import com.juniverse.word.dto.WordDto;
import com.juniverse.word.util.MessageUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.InputEvent;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Ventana del juego Sopa de Letras de 15x15: genera la cuadrícula con las palabras en las ocho direcciones,
 * permite seleccionar arrastrando el ratón, lleva la puntuación y permite cargar listas de palabras.
 *
 * @author ajuar
 */
public class WordsFrame extends JFrame {

    private static final Logger log
            = Logger.getLogger(WordsFrame.class.getName());

    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final String WORDS_FILE = "palabras.txt";

    private static final String FONT_ARIAL = "Arial";
    private static final String LABEL_SCORE = "Score: 0";

    private final char[][] grid;
    private final JButton[][] buttons;
    private List<WordDto> words;
    private List<Point> selection;
    private Point startingPoint;
    private JLabel lblPunctuation;
    private JPanel wordsPanel;
    private int punctuation;
    private final Set<String> wordsFound;

    /** Carga las palabras, genera la sopa, crea la interfaz y muestra la ventana. */
    public WordsFrame() {
        setTitle("Word Search");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        grid = new char[GRID_SIZE][GRID_SIZE];
        buttons = new JButton[GRID_SIZE][GRID_SIZE];
        words = new ArrayList<>();
        selection = new ArrayList<>();
        punctuation = 0;
        wordsFound = new HashSet<>();

        initializeWords();
        generateSoup();
        createInterface();

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    /** Lee {@code palabras.txt}; si no existe o está vacío usa diez palabras de ejemplo y crea el archivo. */
    private void initializeWords() {
        try {

            List<String> lines = Files.readAllLines(Paths.get(WORDS_FILE), StandardCharsets.UTF_8);

            for (String line : lines) {
                line = line.trim().toUpperCase().replaceAll("\\s+", ""); // sin espacios: se confundirían con celdas vacías

                if (!line.isEmpty() && !line.startsWith("#")) {
                    words.add(new WordDto(line));
                }
            }

            if (words.isEmpty()) {
                throw new IOException("The file does not contain valid words");
            }

            log.log(Level.INFO, "{0} words were loaded from " + WORDS_FILE, words.size());

        } catch (IOException e) {

            log.log(Level.WARNING, "The file " + WORDS_FILE + " could not be read: {0}", e.getMessage());
            log.warning("Using default words...");

            words.add(new WordDto("JAVA"));
            words.add(new WordDto("SWING"));
            words.add(new WordDto("PROGRAMA"));
            words.add(new WordDto("CODIGO"));
            words.add(new WordDto("CLASE"));
            words.add(new WordDto("OBJETO"));
            words.add(new WordDto("METODO"));
            words.add(new WordDto("VARIABLE"));
            words.add(new WordDto("INTERFAZ"));
            words.add(new WordDto("HERENCIA"));

            try {
                createExampleFile();
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Could not create sample file: {0}", ex.getMessage());
            }
        }
    }

    /**
     * Crea {@code palabras.txt} con las palabras de ejemplo.
     * @throws IOException si no se puede escribir
     */
    private void createExampleFile() throws IOException {
        List<String> linesExample = Arrays.asList(
                "# Archivo de palabras para Sopa de Letras",
                "# Una palabra por línea",
                "# Las líneas que empiezan con # son comentarios",
                "",
                "JAVA",
                "SWING",
                "PROGRAMA",
                "CODIGO",
                "CLASE",
                "OBJETO",
                "METODO",
                "VARIABLE",
                "INTERFAZ",
                "HERENCIA"
        );

        Files.write(Paths.get(WORDS_FILE), linesExample, StandardCharsets.UTF_8);
        log.info("A sample file was created: " + WORDS_FILE);
    }

    /** Generador de números aleatorios. */
    private Random getRandom() {
        return new Random();
    }

    /**
     * Coloca cada palabra en un lugar y dirección aleatorios (hasta 1000 intentos) y rellena el resto con letras al azar.
     * Descarta las palabras que no caben.
     */
    private void generateSoup() {

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = ' ';
            }
        }

        Random random = getRandom();
        for (WordDto word : words) {
            word.posiciones = null; // posiciones de una partida anterior
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 1000) {
                int fila = random.nextInt(GRID_SIZE);
                int col = random.nextInt(GRID_SIZE);
                int direccion = random.nextInt(8);

                if (canPlaceWord(word.texto, fila, col, direccion)) {
                    placeWord(word, fila, col, direccion);
                    placed = true;
                }
                attempts++;
            }
        }

        // Las palabras que no se pudieron colocar se descartan: si no, el juego no se podría ganar nunca
        List<WordDto> notPlaced = words.stream().filter(w -> w.posiciones == null).toList();
        for (WordDto w : notPlaced) {
            log.log(Level.WARNING, "La palabra {0} no cabe en la cuadrícula y se descarta", w.texto);
            if (w.label != null && wordsPanel != null) {
                wordsPanel.remove(w.label);
            }
        }
        words.removeAll(notPlaced);
        if (wordsPanel != null) {
            wordsPanel.revalidate();
            wordsPanel.repaint();
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == ' ') {
                    grid[i][j] = (char) ('A' + random.nextInt(26));
                }
            }
        }
    }

    /**
     * Comprueba si la palabra cabe desde una posición y dirección, sin chocar con letras distintas.
     * @param word palabra
     * @param row fila inicial
     * @param column columna inicial
     * @param address dirección (0-7, las ocho vecinas)
     */
    private boolean canPlaceWord(String word, int row, int column, int address) {
        int[] df = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        int length = word.length();
        int finalRow = row + df[address] * (length - 1);
        int finalColumn = column + dc[address] * (length - 1);

        if (finalRow < 0 || finalRow >= GRID_SIZE
                || finalColumn < 0 || finalColumn >= GRID_SIZE) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            int f = row + df[address] * i;
            int c = column + dc[address] * i;

            if (grid[f][c] != ' ' && grid[f][c] != word.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Escribe la palabra en la cuadrícula y guarda las celdas que ocupa.
     * @param word palabra
     * @param row fila inicial
     * @param column columna inicial
     * @param address dirección (0-7)
     */
    private void placeWord(WordDto word, int row, int column, int address) {
        int[] df = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        word.posiciones = new ArrayList<>();

        for (int i = 0; i < word.texto.length(); i++) {
            int f = row + df[address] * i;
            int c = column + dc[address] * i;
            grid[f][c] = word.texto.charAt(i);
            word.posiciones.add(new Point(f, c));
        }
    }

    /** Construye el menú, la cuadrícula de botones y el panel de palabras. */
    private void createInterface() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuGame = new JMenu("Game");
        menuGame.setMnemonic('J');

        JMenuItem itemNuevoJuego = new JMenuItem("New Game");
        itemNuevoJuego.setMnemonic('N');
        itemNuevoJuego.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        itemNuevoJuego.addActionListener(e -> newGame());

        JMenuItem itemCargarPalabras = new JMenuItem("Load Words");
        itemCargarPalabras.setMnemonic('C');
        itemCargarPalabras.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        itemCargarPalabras.addActionListener(e -> uploadCustomFile());

        JMenuItem itemSalir = new JMenuItem("Exit");
        itemSalir.setMnemonic('S');
        itemSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemSalir.addActionListener(e -> System.exit(0));

        menuGame.add(itemNuevoJuego);
        menuGame.add(itemCargarPalabras);
        menuGame.addSeparator();
        menuGame.add(itemSalir);

        JMenu menuHelp = new JMenu("Help");
        menuHelp.setMnemonic('A');

        JMenuItem itemInstrucciones = new JMenuItem("How to Play");
        itemInstrucciones.setMnemonic('I');
        itemInstrucciones.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        itemInstrucciones.addActionListener(e -> showInstructions());

        JMenuItem itemAcercaDe = new JMenuItem("About");
        itemAcercaDe.setMnemonic('A');
        itemAcercaDe.addActionListener(e -> about());

        menuHelp.add(itemInstrucciones);
        menuHelp.addSeparator();
        menuHelp.add(itemAcercaDe);

        menuBar.add(menuGame);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Word Search", SwingConstants.CENTER);
        title.setFont(new Font(FONT_ARIAL, Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblPunctuation = new JLabel(LABEL_SCORE, SwingConstants.CENTER);
        lblPunctuation.setFont(new Font(FONT_ARIAL, Font.BOLD, 16));

        panelSuperior.add(title, BorderLayout.NORTH);
        panelSuperior.add(lblPunctuation, BorderLayout.CENTER);

        JPanel panelGrilla = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2));
        panelGrilla.setBackground(Color.DARK_GRAY);
        panelGrilla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton boton = new JButton(String.valueOf(grid[i][j]));
                boton.setFont(new Font(FONT_ARIAL, Font.BOLD, 18));
                boton.setFocusPainted(false);
                boton.setBackground(Color.WHITE);
                boton.setForeground(Color.BLACK);
                boton.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                boton.setMargin(new Insets(0, 0, 0, 0));

                final int fila = i;
                final int col = j;

                boton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        startSelection(fila, col);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if ((e.getModifiersEx() & BUTTON1_DOWN_MASK) != 0) {
                            continueSelection(fila, col);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        finalSelection();
                    }
                });

                buttons[i][j] = boton;
                panelGrilla.add(boton);
            }
        }

        JPanel panelDerecho = new JPanel(new BorderLayout());
        JLabel lblPalabras = new JLabel("Words to find:", SwingConstants.CENTER);
        lblPalabras.setFont(new Font(FONT_ARIAL, Font.BOLD, 14));
        lblPalabras.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        wordsPanel = new JPanel();
        wordsPanel.setLayout(new BoxLayout(wordsPanel, BoxLayout.Y_AXIS));
        wordsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (WordDto palabra : words) {
            JLabel lblPalabra = new JLabel(palabra.texto);
            lblPalabra.setFont(new Font(FONT_ARIAL, Font.PLAIN, 14));
            lblPalabra.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            palabra.label = lblPalabra;
            wordsPanel.add(lblPalabra);
        }

        panelDerecho.add(lblPalabras, BorderLayout.NORTH);
        panelDerecho.add(wordsPanel, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelGrilla, BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);
    }

    /**
     * Empieza una selección en la celda pulsada.
     * @param row fila
     * @param column columna
     */
    private void startSelection(int row, int column) {
        clearSelection();
        startingPoint = new Point(row, column);
        selection.add(new Point(row, column));
        buttons[row][column].setBackground(Color.YELLOW);
        buttons[row][column].setForeground(Color.BLACK);
    }

    /**
     * Amplía la selección en línea recta hasta la celda sobre la que está el ratón.
     * @param row fila
     * @param column columna
     */
    private void continueSelection(int row, int column) {
        if (startingPoint == null) {
            return;
        }

        if (!isValidSelection(startingPoint, new Point(row, column))) {
            return;
        }

        clearVisualSelection();
        selection = getPointsOnline(startingPoint, new Point(row, column));

        for (Point p : selection) {
            buttons[p.x][p.y].setBackground(Color.YELLOW);
            buttons[p.x][p.y].setForeground(Color.BLACK);
        }
    }

    /** Al soltar el ratón, comprueba la selección; si es una palabra suma puntos y, si se encontraron todas, muestra el mensaje de victoria. */
    private void finalSelection() {
        if (selection.isEmpty()) {
            return;
        }

        String selectedWord = getSelectedWord();

        if (verifyWord(selectedWord)) {
            markWordFound(selectedWord);
            punctuation += selectedWord.length() * 10;
            lblPunctuation.setText("Score: " + punctuation);

            if (wordsFound.size() == words.size()) {
                MessageUtil.showInfo("Congratulations! You have found all the words.\nFinal score:" + punctuation, "Victory!");
            }
        } else {
            clearVisualSelection();
        }

        startingPoint = null;
    }

    /** Indica si dos celdas están en línea recta (horizontal, vertical o diagonal). */
    private boolean isValidSelection(Point start, Point end) {
        int difFila = Math.abs(end.x - start.x);
        int difCol = Math.abs(end.y - start.y);

        return difFila == 0 || difCol == 0 || difFila == difCol;
    }

    /** Celdas en línea recta entre dos puntos. */
    private List<Point> getPointsOnline(Point start, Point end) {
        List<Point> points = new ArrayList<>();

        int rows = end.x - start.x;
        int columns = end.y - start.y;
        int steps = Math.max(Math.abs(rows), Math.abs(columns));

        int stepFila = rows == 0 ? 0 : rows / Math.abs(rows);
        int stepCol = columns == 0 ? 0 : columns / Math.abs(columns);

        for (int i = 0; i <= steps; i++) {
            points.add(new Point(start.x + stepFila * i, start.y + stepCol * i));
        }

        return points;
    }

    /** Letras de la selección actual. */
    private String getSelectedWord() {
        StringBuilder sb = new StringBuilder();
        for (Point p : selection) {
            sb.append(grid[p.x][p.y]);
        }
        return sb.toString();
    }

    /**
     * Indica si las letras seleccionadas forman una palabra pendiente (en cualquier sentido).
     * @param word letras seleccionadas
     */
    private boolean verifyWord(String word) {
        return findWord(word) != null;
    }

    /**
     * Busca la palabra de la lista que coincide con el texto seleccionado (en cualquier sentido) y aún no se ha encontrado.
     *
     * @param word letras seleccionadas
     * @return la palabra, o {@code null} si no coincide con ninguna pendiente
     */
    private WordDto findWord(String word) {
        String reversed = new StringBuilder(word).reverse().toString();
        for (WordDto w : words) {
            if (!wordsFound.contains(w.texto) && (w.texto.equals(word) || w.texto.equals(reversed))) {
                return w;
            }
        }
        return null;
    }

    /**
     * Marca en verde la selección y la palabra encontrada.
     * @param word letras seleccionadas
     */
    private void markWordFound(String word) {
        WordDto found = findWord(word);
        for (Point p : selection) {
            buttons[p.x][p.y].setBackground(Color.GREEN);
            buttons[p.x][p.y].setForeground(Color.BLACK);
        }
        if (found != null) {
            wordsFound.add(found.texto); // una sola entrada por palabra, para poder compararla con words.size()
            found.label.setText(found.texto);
            found.label.setForeground(Color.GREEN.darker());
        }
        selection.clear();
    }

    /** Vacía la selección lógica. */
    private void clearSelection() {
        selection.clear();
    }

    /** Quita el color amarillo de las celdas seleccionadas. */
    private void clearVisualSelection() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (buttons[i][j].getBackground().equals(Color.YELLOW)) {
                    buttons[i][j].setBackground(Color.WHITE);
                    buttons[i][j].setForeground(Color.BLACK);
                }
            }
        }
    }

    /** Reinicia la puntuación y genera una sopa nueva con las mismas palabras. */
    private void newGame() {
        wordsFound.clear();
        punctuation = 0;
        lblPunctuation.setText(LABEL_SCORE);

        Collections.shuffle(words);
        generateSoup();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                buttons[i][j].setText(String.valueOf(grid[i][j]));
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].setForeground(Color.BLACK);
            }
        }

        for (WordDto p : words) {
            p.label.setText(p.texto);
            p.label.setForeground(Color.BLACK);
        }
    }

    /** Pide un archivo de texto con palabras (una por línea). */
    private void uploadCustomFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Word file");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "Text files (*.txt)";
            }
        });

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            loadWordsFromFile(archivo.getAbsolutePath());
        }
    }

    /**
     * Carga las palabras del archivo y genera una sopa nueva.
     * @param rutaArchivo ruta del archivo .txt
     */
    private void loadWordsFromFile(String rutaArchivo) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(rutaArchivo), StandardCharsets.UTF_8);
            List<WordDto> newWords = new ArrayList<>();

            for (String line : lines) {
                line = line.trim().toUpperCase().replaceAll("\\s+", ""); // sin espacios: se confundirían con celdas vacías
                if (!line.isEmpty() && !line.startsWith("#")) {
                    newWords.add(new WordDto(line));
                }
            }

            if (newWords.isEmpty()) {
                MessageUtil.showError("The file does not contain valid words", "Error");
                return;
            }

            words = newWords;
            wordsFound.clear();
            punctuation = 0;
            lblPunctuation.setText(LABEL_SCORE);

            wordsPanel.removeAll();
            for (WordDto palabra : words) {
                JLabel lblPalabra = new JLabel(palabra.texto);
                lblPalabra.setFont(new Font(FONT_ARIAL, Font.PLAIN, 14));
                lblPalabra.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                palabra.label = lblPalabra;
                wordsPanel.add(lblPalabra);
            }
            wordsPanel.revalidate();
            wordsPanel.repaint();

            generateSoup();

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    buttons[i][j].setText(String.valueOf(grid[i][j]));
                    buttons[i][j].setBackground(Color.WHITE);
                    buttons[i][j].setForeground(Color.BLACK);
                }
            }

            MessageUtil.showInfo(words.size() + " words were loaded successfully", "Success");

        } catch (IOException e) {

            MessageUtil.showError("Error reading file: " + e.getMessage(), "Error");
        }
    }

    /** Muestra la ayuda de cómo jugar. */
    private void showInstructions() {
        String instructions = """
                <html>
                <body style='width: 400px; padding: 10px;'>
                <h2>Cómo Jugar</h2>

                <p><b>Objetivo:</b><br>
                Encontrar todas las palabras ocultas en la sopa de letras.</p>

                <p><b>Instrucciones:</b></p>
                <ol>
                  <li>Las palabras a encontrar se muestran en el panel derecho</li>
                  <li>Haz clic en la primera letra de la palabra</li>
                  <li>Mantén presionado y arrastra hasta la última letra</li>
                  <li>Solo puedes seleccionar en línea recta (horizontal, vertical o diagonal)</li>
                  <li>Suelta el botón del mouse para verificar</li>
                  <li>Las palabras correctas se marcan en verde</li>
                </ol>

                <p><b>Atajos de Teclado:</b></p>
                <ul>
                  <li><b>Ctrl+N</b> - Nuevo Juego</li>
                  <li><b>Ctrl+O</b> - Cargar Palabras</li>
                  <li><b>F1</b> - Mostrar esta ayuda</li>
                  <li><b>Ctrl+Q</b> - Salir</li>
                </ul>

                <p><b>Puntuación:</b><br>
                Cada letra vale 10 puntos. ¡Encuentra todas las palabras!</p>

                <p><b>Nota:</b> Las palabras pueden aparecer en cualquier dirección,<br>
                incluso al revés.</p>
                </body>
                </html>
                """;

        JEditorPane editorPane = new JEditorPane("text/html", instructions);
        editorPane.setEditable(false);
        editorPane.setBackground(UIManager.getColor("Panel.background"));

        JOptionPane.showMessageDialog(this,
                editorPane,
                "Instrucciones - Sopa de Letras",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Muestra el diálogo "Acerca de". */
    private void about() {
        AboutDialog aboutDialog = new AboutDialog(this);
        aboutDialog.setVisible(true);
    }
}
