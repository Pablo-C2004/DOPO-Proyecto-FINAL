package presentation;


// Medidas lógicas de la cancha (ancho, alto, suelo, arcos)
import domain.Field;

// Fábrica de bordes para los botones
import javax.swing.BorderFactory;
// Botón
import javax.swing.JButton;
// Panel contenedor / lienzo de dibujo
import javax.swing.JPanel;
// Grosor y estilo de las líneas que se dibujan
import java.awt.BasicStroke;
// Colores para dibujar y para los componentes
import java.awt.Color;
// Tamaño (ancho y alto) de un componente
import java.awt.Dimension;
// Tipo de letra de los textos
import java.awt.Font;
// Contexto de dibujo básico de Swing
import java.awt.Graphics;
// Contexto de dibujo 2D avanzado (escala, antialias, figuras)
import java.awt.Graphics2D;
// Rectángulo para recortar lo que se sale de la pantalla lógica
import java.awt.Rectangle;
// Opciones de calidad de dibujo (suavizado de bordes)
import java.awt.RenderingHints;
// Forma de un texto, para dibujarle borde
import java.awt.Shape;
// Representación gráfica de un texto con una fuente
import java.awt.font.GlyphVector;
// Adaptador para escuchar cuando el ratón entra o sale de un botón
import java.awt.event.MouseAdapter;
// Evento del ratón
import java.awt.event.MouseEvent;
// Figura de elipse/círculo (balón decorativo de respaldo)
import java.awt.geom.Ellipse2D;
// Imagen en memoria que se puede dibujar (sprites)
import java.awt.image.BufferedImage;

/**
* Pantalla de inicio según el boceto del ciclo 0.
* Se dibuja en coordenadas lógicas (1000 x 500) y se ajusta al tamaño de la
* ventana con Viewport; los botones se reubican y cambian de tamaño también.
*/
public class StartPanel extends JPanel {

    // Ancho de la pantalla de inicio
    public static final int WIDTH = 1000;
    // Alto de la pantalla de inicio
    public static final int HEIGHT = 500;
    // Ancho de cada botón de modo
    private static final int MODE_BUTTON_WIDTH = 200;
    // Alto de cada botón de modo
    private static final int MODE_BUTTON_HEIGHT = 52;
    // Altura (y) de la fila de botones de modo
    private static final int MODE_BUTTON_Y = 418;
    // Separacion horizontal entre los dos botones de modo
    private static final int MODE_BUTTON_GAP = 80;
    // Color del botón cuando el ratón está encima
    private static final Color HOVER = new Color(255, 236, 150);
    // Recuadro lógico del botón Options {x, y, ancho, alto}
    private static final int[] OPTIONS_BOUNDS = {872, 24, 104, 64};
    // Tamaño lógico de letra del botón Options
    private static final int OPTIONS_FONT = 18;
    // Tamaño lógico de letra de los botones de modo
    private static final int MODE_FONT = 20;

    // Ventana principal (a la que se le avisa qué botón se presionó)
    private final DOPOSoccerGUI gui;
    // Botón Options (opciones)
    private JButton optionsButton;
    // Botón Single Player (jugador vs máquina)
    private JButton singlePlayerButton;
    // Botón Multiplayer (jugador vs jugador)
    private JButton multiplayerButton;
    // Botones de modo en el orden en que aparecen
    private JButton[] modes;

    public StartPanel(DOPOSoccerGUI gui) {
        this.gui = gui;
        prepareElements();
        prepareActions();
    }

    private void prepareElements() {
        setLayout(null);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        setBackground(Color.BLACK);

        optionsButton = sketchButton("Opciones", OPTIONS_FONT);
        add(optionsButton);

        singlePlayerButton = sketchButton("Single Player", MODE_FONT);
        multiplayerButton = sketchButton("Multiplayer", MODE_FONT);
        modes = new JButton[] {singlePlayerButton, multiplayerButton};
        for (JButton mode : modes) {
            add(mode);
        }
    }

    /**
     * Ubica los botones según el tamaño actual del panel. Swing lo llama
     * cada vez que la ventana cambia de tamaño.
     */
    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        place(optionsButton, OPTIONS_BOUNDS[0], OPTIONS_BOUNDS[1], OPTIONS_BOUNDS[2], OPTIONS_BOUNDS[3],
                OPTIONS_FONT, w, h);
        int totalWidth = modes.length * MODE_BUTTON_WIDTH + (modes.length - 1) * MODE_BUTTON_GAP;
        int startX = (WIDTH - totalWidth) / 2;
        for (int i = 0; i < modes.length; i++) {
            int x = startX + i * (MODE_BUTTON_WIDTH + MODE_BUTTON_GAP);
            place(modes[i], x, MODE_BUTTON_Y, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT, MODE_FONT, w, h);
        }
    }

    /**
     * Pone un botón en un recuadro lógico convertido a píxeles reales,
     * con la letra y el borde escalados.
     */
    private static void place(JButton button, int x, int y, int bw, int bh, int fontSize, int w, int h) {
        double s = Viewport.scale(w, h);
        int px = (int) Math.round(Viewport.offsetX(w, h) + x * s);
        int py = (int) Math.round(Viewport.offsetY(w, h) + y * s);
        button.setBounds(px, py, (int) Math.round(bw * s), (int) Math.round(bh * s));
        int size = Math.max(8, (int) Math.round(fontSize * s));
        // Solo se cambian letra y borde si cambió el tamaño, para no volver a pedir el layout sin fin
        if (button.getFont().getSize() != size) {
            button.setFont(new Font("SansSerif", Font.BOLD, size));
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, Math.max(1, (int) Math.round(3 * s))));
        }
    }

    private void prepareActions() {
        optionsButton.addActionListener(e -> gui.showOptions());
        singlePlayerButton.addActionListener(e -> gui.optionSinglePlayer());
        multiplayerButton.addActionListener(e -> gui.optionMultiplayer());
    }

    /**
     * Botón.
     */
    private static JButton sketchButton(String text, int size) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, size));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        Viewport.apply(g2, getWidth(), getHeight());
        g2.clip(new Rectangle(0, 0, WIDTH, HEIGHT));
        Scenery.drawStadium(g2, gui.getStage());
        drawPlayers(g2);
        drawBall(g2);
        drawTitle(g2, "DOPO", 120, 78);
        drawTitle(g2, "SOCCER", 120, 140);
        g2.dispose();
    }

    /**
     * Título.
     */
    private void drawTitle(Graphics2D g2, String text, int x, int y) {
        Font font = new Font("SansSerif", Font.BOLD, 58);
        GlyphVector glyphs = font.createGlyphVector(g2.getFontRenderContext(), text);
        Shape shape = glyphs.getOutline(x, y);
        g2.setColor(new Color(255, 205, 40));
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(shape);
    }

    /**
     * Los dos jugadores, uno frente al otro (si las imágenes están disponibles).
     */
    private void drawPlayers(Graphics2D g2) {
        BufferedImage[] p1 = Sprites.frames(Sprites.PLAYER1_IDLE);
        BufferedImage[] p2 = Sprites.frames(Sprites.PLAYER2_IDLE);
        int size = 150;
        int top = MODE_BUTTON_Y - size;
        if (p1 != null) {
            g2.drawImage(p1[0], 230, top, size, size, null);
        }
        if (p2 != null) {
            g2.drawImage(p2[0], 770, top, -size, size, null);
        }
    }

    /**
     * Balón decorativo en el centro, entre los dos jugadores.
     */
    private void drawBall(Graphics2D g2) {
        double r = 26;
        double cx = Field.WIDTH / 2;
        double cy = 330;
        BufferedImage image = Sprites.get(Sprites.BALL);
        if (image != null) {
            g2.drawImage(image, (int) (cx - r), (int) (cy - r), (int) (2 * r), (int) (2 * r), null);
            return;
        }
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g2.fill(new Ellipse2D.Double(cx - 7, cy - 7, 14, 14));
    }
}
