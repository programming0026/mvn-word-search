package com.juniverse.word.gui;


import com.juniverse.word.util.DateUtil;
import com.juniverse.word.util.IconUtil;
import com.juniverse.word.util.MessageUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

/**
 * The Class AboutDialog.
 *
 * @author ajuar
 */
public class AboutDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The Constant APP_NAME.
     */
    private static final String APP_NAME = "Word search";

    /**
     * The Constant VERSION.
     */
    private static final String VERSION = "1.0.0";

    /**
     * The Constant AUTHOR.
     */
    private static final String AUTHOR = "A. Juarez";

    /**
     * The Constant WEBSITE.
     */
    private static final String WEBSITE = "https://github.com/AJuarez0021";

    /**
     * The Constant COPYRIGHT.
     */
    private static final String COPYRIGHT = "© %d All rights reserved";

    /**
     * The Constant FONT_NAME.
     */
    private static final String FONT_NAME = "Arial";

    /**
     * Instantiates a new about dialog.
     *
     * @param parent the parent
     */
    public AboutDialog(Frame parent) {
        super(parent, "About", true);
        initComponents();
    }

    /**
     * Initialize the components.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(IconUtil.loadIcon("/icons/main.png", 64, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(iconLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel appNameLabel = new JLabel(APP_NAME);
        appNameLabel.setFont(new Font(FONT_NAME, Font.BOLD, 24));
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(appNameLabel);
        mainPanel.add(Box.createVerticalStrut(5));

        JLabel versionLabel = new JLabel("Version " + VERSION);
        versionLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(versionLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        JSeparator separator1 = new JSeparator();
        separator1.setMaximumSize(new Dimension(300, 1));
        mainPanel.add(separator1);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel authorLabel = new JLabel("Developed by: " + AUTHOR);
        authorLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(authorLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel websiteLabel = createHyperlinkLabel(WEBSITE, WEBSITE);
        websiteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(websiteLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        JSeparator separator2 = new JSeparator();
        separator2.setMaximumSize(new Dimension(300, 1));
        mainPanel.add(separator2);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel copyrightLabel = new JLabel(String.format(COPYRIGHT, DateUtil.getYear()));
        copyrightLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        copyrightLabel.setForeground(Color.GRAY);
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(copyrightLabel);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 30));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().setDefaultButton(closeButton);
    }

    /**
     * Crea un JLabel con hipervínculo funcional.
     *
     * @param text The text
     * @param url The url
     * @return the j label
     */
    private JLabel createHyperlinkLabel(String text, String url) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(Color.BLUE);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setFont(new Font(FONT_NAME, Font.PLAIN, 13));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openWebpage(url);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(new Color(0, 0, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.BLUE);
            }
        });

        return label;
    }

    /**
     * Abre una URL en el navegador predeterminado.
     *
     * @param url the url
     */
    private void openWebpage(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                } else {
                    showUrlManually(url);
                }
            } else {
                showUrlManually(url);
            }
        } catch (IOException | URISyntaxException e) {
            MessageUtil.showError("The browser could not be opened.\nVisit: " + url, "Error");
        }
    }

    /**
     * Muestra la URL en un diálogo si no se puede abrir automáticamente.
     *
     * @param url the url
     */
    private void showUrlManually(String url) {

        MessageUtil.showInfo("Please visit:\n" + url, "Website");
    }

    /**
     * Crea un icono para la aplicación Puedes reemplazar esto con tu propio
     * icono.
     *
     * @return the image icon
     */
    public ImageIcon createAppIcon() {

        int size = 64;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRoundRect(12, 8, 40, 48, 8, 8);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(20, 20, 24, 3);
        g2d.fillRect(20, 28, 24, 3);
        g2d.fillRect(20, 36, 24, 3);

        g2d.dispose();

        return new ImageIcon(image);
    }
}
