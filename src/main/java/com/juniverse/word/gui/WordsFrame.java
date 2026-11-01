package com.juniverse.word.gui;

import com.juniverse.word.dto.WordDto;
import com.juniverse.word.model.LineSelection;
import com.juniverse.word.model.WordListParser;
import com.juniverse.word.model.WordSearchGrid;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
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
    /** Lista de palabras incluida en {@code src/main/resources}. */
    private static final String WORDS_RESOURCE = "/palabras.txt";
    /** Palabras que se usan si el recurso falta o no tiene ninguna válida. */
    private static final List<String> DEFAULT_WORDS = List.of("JAVA", "SWING", "PROGRAMA", "CODIGO", "CLASE",
            "OBJETO", "METODO", "VARIABLE", "INTERFAZ", "HERENCIA");

    private static final String FONT_ARIAL = "Arial";
    private static final String LABEL_SCORE = "Score: 0";

    private final WordSearchGrid grid;
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

        grid = new WordSearchGrid(GRID_SIZE, new Random());
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

    /** Lee {@code palabras.txt} de la carpeta {@code resources} (va dentro del jar); si falta o está vacío usa diez palabras de ejemplo. */
    private void initializeWords() {
        try (InputStream in = WordsFrame.class.getResourceAsStream(WORDS_RESOURCE)) {
            if (in == null) {
                throw new IOException("Resource not found: " + WORDS_RESOURCE);
            }

            List<String> lines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().toList();

            for (String line : WordListParser.parse(lines)) {
                words.add(new WordDto(line));
            }

            if (words.isEmpty()) {
                throw new IOException("The file does not contain valid words");
            }

            log.log(Level.INFO, "{0} words were loaded from " + WORDS_RESOURCE, words.size());

        } catch (IOException e) {

            log.log(Level.WARNING, "The resource " + WORDS_RESOURCE + " could not be read: {0}", e.getMessage());
            log.warning("Using default words...");

            for (String word : DEFAULT_WORDS) {
                words.add(new WordDto(word));
            }
        }
    }

    /**
     * Genera una sopa nueva con las palabras actuales y descarta las que no caben (si no, el juego no se podría ganar).
     */
    private void generateSoup() {
        List<WordDto> notPlaced = grid.generate(words);
        for (WordDto w : notPlaced) {
            log.log(Level.WARNING, "La palabra {0} no cabe en la cuadrícula y se descarta", w.text);
            if (w.label != null && wordsPanel != null) {
                wordsPanel.remove(w.label);
            }
        }
        words.removeAll(notPlaced);
        if (wordsPanel != null) {
            wordsPanel.revalidate();
            wordsPanel.repaint();
        }
    }

    /** Construye el menú, la cuadrícula de botones y el panel de palabras. */
    private void createInterface() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuGame = new JMenu("Game");
        menuGame.setMnemonic('J');

        JMenuItem itemNewGame = new JMenuItem("New Game");
        itemNewGame.setMnemonic('N');
        itemNewGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        itemNewGame.addActionListener(e -> newGame());

        JMenuItem itemLoadWords = new JMenuItem("Load Words");
        itemLoadWords.setMnemonic('C');
        itemLoadWords.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        itemLoadWords.addActionListener(e -> uploadCustomFile());

        JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.setMnemonic('S');
        itemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemExit.addActionListener(e -> System.exit(0));

        menuGame.add(itemNewGame);
        menuGame.add(itemLoadWords);
        menuGame.addSeparator();
        menuGame.add(itemExit);

        JMenu menuHelp = new JMenu("Help");
        menuHelp.setMnemonic('A');

        JMenuItem itemInstructions = new JMenuItem("How to Play");
        itemInstructions.setMnemonic('I');
        itemInstructions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        itemInstructions.addActionListener(e -> showInstructions());

        JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.setMnemonic('A');
        itemAbout.addActionListener(e -> about());

        menuHelp.add(itemInstructions);
        menuHelp.addSeparator();
        menuHelp.add(itemAbout);

        menuBar.add(menuGame);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Word Search", SwingConstants.CENTER);
        title.setFont(new Font(FONT_ARIAL, Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblPunctuation = new JLabel(LABEL_SCORE, SwingConstants.CENTER);
        lblPunctuation.setFont(new Font(FONT_ARIAL, Font.BOLD, 16));

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(lblPunctuation, BorderLayout.CENTER);

        JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2));
        gridPanel.setBackground(Color.DARK_GRAY);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton(String.valueOf(grid.get(i, j)));
                button.setFont(new Font(FONT_ARIAL, Font.BOLD, 18));
                button.setFocusPainted(false);
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setMargin(new Insets(0, 0, 0, 0));

                final int row = i;
                final int col = j;

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        startSelection(row, col);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if ((e.getModifiersEx() & BUTTON1_DOWN_MASK) != 0) {
                            continueSelection(row, col);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        finalSelection();
                    }
                });

                buttons[i][j] = button;
                gridPanel.add(button);
            }
        }

        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel lblWords = new JLabel("Words to find:", SwingConstants.CENTER);
        lblWords.setFont(new Font(FONT_ARIAL, Font.BOLD, 14));
        lblWords.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        wordsPanel = new JPanel();
        wordsPanel.setLayout(new BoxLayout(wordsPanel, BoxLayout.Y_AXIS));
        wordsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (WordDto word : words) {
            JLabel lblWord = new JLabel(word.text);
            lblWord.setFont(new Font(FONT_ARIAL, Font.PLAIN, 14));
            lblWord.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            word.label = lblWord;
            wordsPanel.add(lblWord);
        }

        rightPanel.add(lblWords, BorderLayout.NORTH);
        rightPanel.add(wordsPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
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

        if (!LineSelection.isStraightLine(startingPoint, new Point(row, column))) {
            return;
        }

        clearVisualSelection();
        selection = LineSelection.pointsBetween(startingPoint, new Point(row, column));

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

    /** Letras de la selección actual. */
    private String getSelectedWord() {
        return grid.lettersAt(selection);
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
        return LineSelection.findPending(words, wordsFound, word);
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
            wordsFound.add(found.text); // una sola entrada por palabra, para poder compararla con words.size()
            found.label.setText(found.text);
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
                buttons[i][j].setText(String.valueOf(grid.get(i, j)));
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].setForeground(Color.BLACK);
            }
        }

        for (WordDto p : words) {
            p.label.setText(p.text);
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
            File file = fileChooser.getSelectedFile();
            loadWordsFromFile(file.getAbsolutePath());
        }
    }

    /**
     * Carga las palabras del archivo y genera una sopa nueva.
     * @param filePath ruta del archivo .txt
     */
    private void loadWordsFromFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            List<WordDto> newWords = new ArrayList<>();

            for (String line : WordListParser.parse(lines)) {
                newWords.add(new WordDto(line));
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
            for (WordDto word : words) {
                JLabel lblWord = new JLabel(word.text);
                lblWord.setFont(new Font(FONT_ARIAL, Font.PLAIN, 14));
                lblWord.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                word.label = lblWord;
                wordsPanel.add(lblWord);
            }
            wordsPanel.revalidate();
            wordsPanel.repaint();

            generateSoup();

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    buttons[i][j].setText(String.valueOf(grid.get(i, j)));
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
