package presentation;

// Fábrica de bordes
import javax.swing.BorderFactory;
// Botón
import javax.swing.JButton;
// Lista desplegable
import javax.swing.JComboBox;
// Componente base de Swing
import javax.swing.JComponent;
// Ventana de diálogo
import javax.swing.JDialog;
// Etiqueta de texto
import javax.swing.JLabel;
// Lista (la que muestra el desplegable abierto)
import javax.swing.JList;
// Barra de menú
import javax.swing.JMenuBar;
// Panel contenedor / lienzo de dibujo
import javax.swing.JPanel;
// Botón de opción
import javax.swing.JRadioButton;
// Campo numérico con flechas
import javax.swing.JSpinner;
// Campo de texto con formato (el que va dentro del spinner)
import javax.swing.JFormattedTextField;
// Tecla (para cerrar con ESC)
import javax.swing.KeyStroke;
// Acción asociada a una tecla
import javax.swing.AbstractAction;
// Constantes de dirección y alineación
import javax.swing.SwingConstants;
// Utilidades de Swing (buscar la ventana de un componente)
import javax.swing.SwingUtilities;
// Valores por defecto del aspecto de Swing
import javax.swing.UIManager;
// Recursos de color para UIManager
import javax.swing.plaf.ColorUIResource;
// Recursos de letra para UIManager
import javax.swing.plaf.FontUIResource;
// Pintor de celdas por defecto para listas
import javax.swing.DefaultListCellRenderer;
// Icono dibujado a mano
import javax.swing.Icon;
// Aspecto básico del desplegable (para cambiar su flecha)
import javax.swing.plaf.basic.BasicComboBoxUI;
// Aspecto básico del spinner (para cambiar sus flechas)
import javax.swing.plaf.basic.BasicSpinnerUI;
// Grosor de líneas
import java.awt.BasicStroke;
// Distribución en zonas
import java.awt.BorderLayout;
// Colores
import java.awt.Color;
// Componente genérico de AWT
import java.awt.Component;
// Cursor del ratón
import java.awt.Cursor;
// Tamaño de un componente
import java.awt.Dimension;
// Distribución en fila
import java.awt.FlowLayout;
// Tipo de letra
import java.awt.Font;
// Medidas de un texto
import java.awt.FontMetrics;
// Contexto de dibujo básico
import java.awt.Graphics;
// Contexto de dibujo 2D
import java.awt.Graphics2D;
// Distribución de un panel
import java.awt.LayoutManager;
// Punto (para arrastrar la ventana)
import java.awt.Point;
// Opciones de calidad de dibujo
import java.awt.RenderingHints;
// Forma de un texto, para dibujarle borde
import java.awt.Shape;
// Ventana genérica
import java.awt.Window;
// Evento de acción
import java.awt.event.ActionEvent;
// Código de las teclas
import java.awt.event.KeyEvent;
// Adaptador del ratón
import java.awt.event.MouseAdapter;
// Evento del ratón
import java.awt.event.MouseEvent;
// Representación gráfica de un texto
import java.awt.font.GlyphVector;

/**
 * Estilo visual común de las ventanas del juego.
 */
public final class GameStyle {

    // Amarillo de los títulos (el mismo de "DOPO SOCCER")
    public static final Color GOLD = new Color(255, 205, 40);
    // Amarillo claro al pasar el ratón (el mismo de los botones del inicio)
    public static final Color HOVER = new Color(255, 236, 150);
    // Negro de bordes y textos
    public static final Color INK = Color.BLACK;
    // Blanco de los recuadros
    public static final Color PAPER = Color.WHITE;
    // Gris de los botones deshabilitados
    public static final Color DISABLED = new Color(205, 205, 205);
    // Verde oscuro de las franjas del césped
    public static final Color GRASS_DARK = new Color(46, 125, 50);
    // Verde claro de las franjas del césped
    public static final Color GRASS_LIGHT = new Color(67, 150, 64);
    // Fondo de la barra de título de las ventanas
    public static final Color TITLE_BAR = new Color(25, 25, 30);
    // Ancho de cada franja del césped
    private static final int STRIPE = 36;
    // Redondeo de las esquinas de los recuadros
    private static final int ARC = 14;
    // Tipo de letra del juego
    private static final String FONT_NAME = "SansSerif";

    private GameStyle() {
    }

    /**
     * Letra en negrilla del juego.
     */
    public static Font font(int size) {
        return new Font(FONT_NAME, Font.BOLD, size);
    }

    /**
     * Cambia los valores por defecto de Swing para que los menús desplegables
     * (Archivo) usen los colores del juego. Se llama una vez, antes de crear
     * la ventana principal.
     */
    public static void install() {
        UIManager.put("Menu.font", new FontUIResource(font(14)));
        UIManager.put("MenuItem.font", new FontUIResource(font(14)));
        UIManager.put("Menu.selectionBackground", new ColorUIResource(HOVER));
        UIManager.put("Menu.selectionForeground", new ColorUIResource(INK));
        UIManager.put("MenuItem.background", new ColorUIResource(PAPER));
        UIManager.put("MenuItem.foreground", new ColorUIResource(INK));
        UIManager.put("MenuItem.selectionBackground", new ColorUIResource(HOVER));
        UIManager.put("MenuItem.selectionForeground", new ColorUIResource(INK));
        UIManager.put("MenuItem.acceleratorForeground", new ColorUIResource(INK));
        UIManager.put("PopupMenu.background", new ColorUIResource(PAPER));
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(INK, 2));
        UIManager.put("Separator.foreground", new ColorUIResource(INK));
        UIManager.put("Separator.background", new ColorUIResource(PAPER));
    }
    // Ventanas
    /**
     * Arma una ventana con el estilo del juego.
     */
    public static void decorate(JDialog dialog, String title, JComponent body, JComponent buttons) {
        dialog.setUndecorated(true);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(INK, 4));
        root.add(new TitleBar(dialog, title), BorderLayout.NORTH);

        JPanel center = new GrassPanel(new BorderLayout(0, 12));
        center.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        center.add(body, BorderLayout.CENTER);
        if (buttons != null) {
            buttons.setOpaque(false);
            center.add(buttons, BorderLayout.SOUTH);
        }
        root.add(center, BorderLayout.CENTER);
        dialog.setContentPane(root);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        root.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
    }

    /**
     * Fila de botones alineada a la derecha.
     */
    public static JPanel buttonRow(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);
        for (JButton b : buttons) {
            row.add(b);
        }
        return row;
    }

    /**
     * formularios.
     */
    public static JPanel card(LayoutManager layout) {
        JPanel card = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = smooth(g);
                g2.setColor(PAPER);
                g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
                g2.setColor(INK);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        return card;
    }

    /**
     * Título para usar dentro de una tarjeta o encima del césped.
     */
    public static JComponent title(String text, int size) {
        return new OutlinedText(text, size);
    }
    //Componentes
    /**
     * Etiqueta en negrilla negra.
     */
    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(15));
        label.setForeground(INK);
        return label;
    }

    /**
     * Botón blanco con borde negro que se pone amarillo claro con el ratón.
     */
    public static JButton button(String text) {
        return new GameButton(text, PAPER);
    }

    /**
     * Botón principal (Jugar, Aceptar...)
     */
    public static JButton primaryButton(String text) {
        return new GameButton(text, GOLD);
    }

    /**
     * Lista desplegable con el estilo del juego.
     */
    public static <T> JComboBox<T> comboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new ArrowButton(SwingConstants.SOUTH);
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
                // Swing usaría el gris por defecto; se pinta blanco como los recuadros
                g.setColor(PAPER);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        box.setFont(font(15));
        box.setBackground(PAPER);
        box.setForeground(INK);
        box.setBorder(BorderFactory.createLineBorder(INK, 2));
        // Sin foco para que el valor no se pinte con el azul de selección de Swing
        box.setFocusable(false);
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(font(15));
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                setBackground(isSelected && index >= 0 ? HOVER : PAPER);
                setForeground(INK);
                return this;
            }
        });
        return box;
    }

    /**
     * Botón de opción con círculo blanco que se rellena de amarillo al marcarlo.
     */
    public static JRadioButton radioButton(String text, boolean selected) {
        JRadioButton radio = new JRadioButton(text, selected);
        radio.setOpaque(false);
        radio.setFont(font(15));
        radio.setForeground(INK);
        radio.setFocusPainted(false);
        radio.setIcon(new RadioIcon(false));
        radio.setSelectedIcon(new RadioIcon(true));
        radio.setIconTextGap(6);
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return radio;
    }

    /**
     * Le da el estilo del juego a un spinner (flechas amarillas y borde negro).
     */
    public static void styleSpinner(JSpinner spinner) {
        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                ArrowButton b = new ArrowButton(SwingConstants.NORTH);
                b.setName("Spinner.nextButton");
                installNextButtonListeners(b);
                return b;
            }

            @Override
            protected Component createPreviousButton() {
                ArrowButton b = new ArrowButton(SwingConstants.SOUTH);
                b.setName("Spinner.previousButton");
                installPreviousButtonListeners(b);
                return b;
            }
        });
        spinner.setBorder(BorderFactory.createLineBorder(INK, 2));
        styleSpinnerEditor(spinner);
    }

    /**
     * Vuelve a darle estilo al campo de texto del spinner. Hay que llamarlo
     * después de setModel(), porque Swing crea un campo nuevo.
     */
    public static void styleSpinnerEditor(JSpinner spinner) {
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JFormattedTextField field = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            field.setFont(font(15));
            field.setBackground(PAPER);
            field.setForeground(INK);
            field.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            field.setHorizontalAlignment(JFormattedTextField.CENTER);
            field.setColumns(4);
        }
    }

    /**
     * Barra de menú verde oscuro con letras blancas.
     */
    public static JMenuBar menuBar() {
        JMenuBar bar = new JMenuBar() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(TITLE_BAR);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(GOLD);
                g.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
        };
        bar.setBorder(BorderFactory.createEmptyBorder(2, 4, 5, 4));
        return bar;
    }

    // Mensajes

    /**
     * Mensaje con un solo botón "Aceptar" (reemplaza a JOptionPane).
     */
    public static void message(Component parent, String text, String title) {
        ask(parent, text, title, false);
    }

    /**
     * Pregunta de sí o no. Devuelve true si el usuario escogió "Sí".
     */
    public static boolean confirm(Component parent, String text, String title) {
        return ask(parent, text, title, true);
    }

    private static boolean ask(Component parent, String text, String title, boolean yesNo) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        boolean[] answer = {false};

        JPanel body = card(new BorderLayout());
        JLabel label = label(toHtml(text));
        label.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        body.add(label, BorderLayout.CENTER);

        JPanel buttons;
        if (yesNo) {
            JButton yes = primaryButton("Sí");
            JButton no = button("No");
            yes.addActionListener(e -> {
                answer[0] = true;
                dialog.dispose();
            });
            no.addActionListener(e -> dialog.dispose());
            buttons = buttonRow(no, yes);
            dialog.getRootPane().setDefaultButton(yes);
        } else {
            JButton ok = primaryButton("Aceptar");
            ok.addActionListener(e -> dialog.dispose());
            buttons = buttonRow(ok);
            dialog.getRootPane().setDefaultButton(ok);
        }
        decorate(dialog, title, body, buttons);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return answer[0];
    }

    /**
     * Convierte el texto a HTML para que respete los saltos de línea y
     * parta las líneas largas.
     */
    private static String toHtml(String text) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
        String width = text.length() > 60 ? " style='width:320px'" : "";
        return "<html><div" + width + ">" + safe + "</div></html>";
    }

    /**
     * Contexto de dibujo con bordes suavizados.
     */
    private static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    /**
     * Dibuja un texto amarillo con contorno negro.
     */
    private static void drawOutlined(Graphics2D g2, String text, Font font, float x, float y, float stroke) {
        GlyphVector glyphs = font.createGlyphVector(g2.getFontRenderContext(), text);
        Shape shape = glyphs.getOutline(x, y);
        g2.setColor(INK);
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(shape);
        g2.setColor(GOLD);
        g2.fill(shape);
    }

    // Clases internas

    /**
     * Panel con franjas verdes, como el césped de la cancha.
     */
    private static class GrassPanel extends JPanel {
        GrassPanel(LayoutManager layout) {
            super(layout);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            for (int x = 0, i = 0; x < getWidth(); x += STRIPE, i++) {
                g.setColor(i % 2 == 0 ? GRASS_DARK : GRASS_LIGHT);
                g.fillRect(x, 0, STRIPE, getHeight());
            }
        }
    }

    /**
     * Barra de título propia: título amarillo, botón X y se puede arrastrar
     * para mover la ventana.
     */
    private static class TitleBar extends JPanel {
        private final String title;
        private Point grab;

        TitleBar(JDialog dialog, String title) {
            super(new BorderLayout());
            this.title = title;
            setOpaque(true);
            setPreferredSize(new Dimension(10, 46));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, INK),
                    BorderFactory.createEmptyBorder(8, 0, 8, 10)));

            JButton close = new GameButton("X", PAPER);
            close.setFont(font(14));
            close.setBorder(BorderFactory.createEmptyBorder(2, 9, 2, 9));
            close.addActionListener(e -> dialog.dispose());
            add(close, BorderLayout.EAST);

            MouseAdapter drag = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    grab = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (grab != null) {
                        Point p = dialog.getLocation();
                        dialog.setLocation(p.x + e.getX() - grab.x, p.y + e.getY() - grab.y);
                    }
                }
            };
            addMouseListener(drag);
            addMouseMotionListener(drag);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(TITLE_BAR);
            g2.fillRect(0, 0, getWidth(), getHeight());
            drawOutlined(g2, title.toUpperCase(), font(24), 14, getHeight() - 15, 4);
            g2.dispose();
        }
    }

    /**
     * Texto amarillo con contorno como componente.
     */
    private static class OutlinedText extends JComponent {
        private final String text;
        private final Font font;

        OutlinedText(String text, int size) {
            this.text = text;
            this.font = font(size);
            FontMetrics fm = getFontMetrics(font);
            setPreferredSize(new Dimension(fm.stringWidth(text) + 10, fm.getHeight() + 4));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            FontMetrics fm = g2.getFontMetrics(font);
            drawOutlined(g2, text, font, 4, fm.getAscent() + 1, 3.5f);
            g2.dispose();
        }
    }

    /**
     * Botón dibujado a mano: recuadro de color con borde negro grueso.
     */
    private static class GameButton extends JButton {
        private final Color base;

        GameButton(String text, Color base) {
            super(text);
            this.base = base;
            setFont(font(15));
            setForeground(INK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            Color fill = base;
            if (!isEnabled()) {
                fill = DISABLED;
            } else if (getModel().isPressed()) {
                fill = GOLD.darker();
            } else if (getModel().isRollover()) {
                fill = HOVER;
            }
            int w = getWidth() - 3;
            int h = getHeight() - 3;
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, w, h, 10, 10);
            g2.setColor(INK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(1, 1, w, h, 10, 10);
            g2.setFont(getFont());
            g2.setColor(isEnabled() ? INK : new Color(110, 110, 110));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }

    /**
     * Botón amarillo con un triángulo negro (flechas del desplegable y del spinner).
     */
    private static class ArrowButton extends JButton {
        private final int direction;

        ArrowButton(int direction) {
            this.direction = direction;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setPreferredSize(new Dimension(24, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(getModel().isRollover() ? HOVER : GOLD);
            g2.fillRect(0, 0, w, h);
            g2.setColor(INK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(0, 0, 0, h);
            if (direction == SwingConstants.SOUTH && h < 20) {
                g2.drawLine(0, 0, w, 0);
            }
            int s = Math.max(4, Math.min(w, h) / 3);
            int cx = w / 2;
            int cy = h / 2;
            int[] xs = {cx - s, cx + s, cx};
            int[] ys = direction == SwingConstants.NORTH
                    ? new int[] {cy + s / 2, cy + s / 2, cy - s / 2 - 1}
                    : new int[] {cy - s / 2, cy - s / 2, cy + s / 2 + 1};
            g2.fillPolygon(xs, ys, 3);
            g2.dispose();
        }
    }

    /**
     * Círculo de los botones de opción.
     */
    private static class RadioIcon implements Icon {
        private final boolean selected;

        RadioIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = smooth(g);
            g2.setColor(PAPER);
            g2.fillOval(x + 1, y + 1, 16, 16);
            if (selected) {
                g2.setColor(GOLD);
                g2.fillOval(x + 4, y + 4, 10, 10);
                g2.setColor(INK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x + 4, y + 4, 10, 10);
            }
            g2.setColor(INK);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(x + 1, y + 1, 16, 16);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }
}
