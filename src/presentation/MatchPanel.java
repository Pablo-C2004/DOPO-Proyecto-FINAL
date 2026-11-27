package presentation;

// Balón del partido (posición, velocidad, tiro de poder)
import domain.Ball;
// Clase principal del dominio: el partido
import domain.DOPOSoccer;
// Efecto que un tiro de poder deja sobre el rival
import domain.Effect;
// Medidas lógicas de la cancha (ancho, alto, suelo, arcos)
import domain.Field;
// Jugador: estadísticas, puntos y estado en el partido
import domain.Player;
// Estadísticas del jugador (speed, kick, jump, survival)
import domain.Stats;
// Regla de partido por tiempo (con muerte súbita)
import domain.TimeRule;

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
// Medidas de un texto para centrarlo o alinearlo
import java.awt.FontMetrics;
// Contexto de dibujo básico de Swing
import java.awt.Graphics;
// Contexto de dibujo 2D avanzado (escala, antialias, figuras)
import java.awt.Graphics2D;
// Rectángulo para recortar lo que se sale de la cancha
import java.awt.Rectangle;
// Opciones de calidad de dibujo (suavizado de bordes)
import java.awt.RenderingHints;
// Transformación geométrica (traslación) para dibujar el balón
import java.awt.geom.AffineTransform;
// Figura de elipse/círculo (cabezas, balones, aura)
import java.awt.geom.Ellipse2D;
// Rectángulo con bordes redondeados (cuerpo, banda)
import java.awt.geom.RoundRectangle2D;
// Imagen en memoria que se puede dibujar (sprites)
import java.awt.image.BufferedImage;
// Transparencia al dibujar (jugador aturdido)
import java.awt.AlphaComposite;
// Combinación de colores al dibujar (para restaurar la transparencia)
import java.awt.Composite;

/**
 * Pantalla del partido según el boceto del ciclo 0.
 * Los jugadores, el pie y el balón usan los sprites de la carpeta images/.
 * Dibuja en coordenadas lógicas (1000 x 500) y se estira o encoge para llenar
 * el panel (ver Viewport), así funciona con cualquier tamaño de ventana.
 * Claude creo la implementación de las imagenes, el apartado grafico fue sacado de OpenGameArt
 */
public class MatchPanel extends JPanel {

    // Acción de hacer clic en el botón de pausa
    public static final String PAUSE = "pause";
    // Acción de hacer clic en "Continuar" del menú de pausa
    public static final String RESUME = "resume";
    // Acción de hacer clic en "Opciones" del menú de pausa
    public static final String OPTIONS = "options";
    // Acción de hacer clic en "Terminar partido" del menú de pausa
    public static final String FINISH = "finish";

    // Recuadro de la barra de poder del jugador 1 {x, y, ancho, alto}
    private static final int[] POWER_P1 = {30, 14, 150, 40};
    // Recuadro de la barra de poder del jugador 2
    private static final int[] POWER_P2 = {790, 14, 150, 40};
    // Recuadro de la bandera del jugador 1
    private static final int[] FLAG_P1 = {220, 12, 96, 62};
    // Recuadro de la bandera del jugador 2
    private static final int[] FLAG_P2 = {650, 12, 96, 62};
    // Recuadro del marcador del jugador 1 (S1)
    private static final int[] SCORE_P1 = {318, 58, 36, 36};
    // Recuadro del marcador del jugador 2 (S2)
    private static final int[] SCORE_P2 = {748, 58, 36, 36};
    // Recuadro del reloj del partido
    private static final int[] TIMER = {390, 10, 220, 72};
    // Centro x del botón de pausa
    private static final int PAUSE_X = 972;
    // Centro y del botón de pausa
    private static final int PAUSE_Y = 34;
    // Radio del botón de pausa
    private static final int PAUSE_R = 20;
    // Botón "Continuar" del menú de pausa
    private static final int[] RESUME_BUTTON = {380, 250, 240, 50};
    // Botón "Opciones" del menú de pausa (abre la misma ventana que en el inicio)
    private static final int[] OPTIONS_BUTTON = {380, 315, 240, 50};
    // Botón "Terminar partido" del menú de pausa
    private static final int[] FINISH_BUTTON = {380, 380, 240, 50};

    // Color de fondo de los recuadros del marcador
    private static final Color BOX = new Color(255, 255, 255, 235);
    // Color de la barra de poder
    private static final Color POWER = new Color(255, 200, 40);
    // Color del fondo de las barras
    private static final Color BAR_BACK = new Color(200, 200, 210);
    // Color de la barra de vida
    private static final Color LIFE = new Color(70, 200, 90);
    // Color de la barra de vida cuando está baja
    private static final Color LIFE_LOW = new Color(220, 70, 60);
    // Color translúcido del aura
    private static final Color AURA = new Color(255, 220, 60, 110);
    // Fondo oscuro translúcido detrás de los textos blancos
    private static final Color LABEL_BACKGROUND = new Color(0, 0, 0, 140);
    // Tamaño (lado) con el que se dibuja el sprite del jugador
    private static final int SPRITE_SIZE = 104;
    // Ancho con el que se dibuja el pie al patear
    private static final int FOOT_WIDTH = 34;
    // Alto con el que se dibuja el pie al patear
    private static final int FOOT_HEIGHT = 18;
    // Ancho de la bandera pequeña sobre la cabeza
    private static final int MINI_FLAG_WIDTH = 22;
    // Alto de la bandera pequeña sobre la cabeza
    private static final int MINI_FLAG_HEIGHT = 14;

    // Juego que se dibuja
    private final DOPOSoccer game;
    // Escenario (fondo) de la cancha; se puede cambiar desde Options
    private Stage stage;

    public MatchPanel(DOPOSoccer game, Stage stage) {
        this.game = game;
        this.stage = stage;
        setPreferredSize(new Dimension((int) Field.WIDTH, (int) Field.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(false);
    }

    /**
     * Cambia el escenario del partido en curso.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        repaint();
    }

    /**
     * Indica qué control hay en un punto de la pantalla (en píxeles).
     * @return PAUSE, RESUME, OPTIONS, FINISH o null si no hay ningún control
     */
    public String actionAt(int px, int py) {
        double x = Viewport.toLogicalX(px, getWidth(), getHeight());
        double y = Viewport.toLogicalY(py, getWidth(), getHeight());
        if (Math.hypot(x - PAUSE_X, y - PAUSE_Y) <= PAUSE_R) {
            return PAUSE;
        }
        if (game.isPaused()) {
            if (inside(x, y, RESUME_BUTTON)) {
                return RESUME;
            }
            if (inside(x, y, OPTIONS_BUTTON)) {
                return OPTIONS;
            }
            if (inside(x, y, FINISH_BUTTON)) {
                return FINISH;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        Viewport.apply(g2, getWidth(), getHeight());
        g2.clip(new Rectangle(0, 0, (int) Field.WIDTH, (int) Field.HEIGHT));
        Scenery.drawStadium(g2, stage);
        drawHud(g2);
        drawPlayer(g2, game.getPlayer(1), 1);
        drawPlayer(g2, game.getPlayer(2), 2);
        for (Ball ball : game.getBalls()) {
            drawBall(g2, ball);
        }
        if (game.isPaused()) {
            drawPauseMenu(g2);
        }
        g2.dispose();
    }
    // Marcador superior
    private void drawHud(Graphics2D g2) {
        Player p1 = game.getPlayer(1);
        Player p2 = game.getPlayer(2);
        drawPower(g2, p1, POWER_P1, "PODER J1", "E", false);
        drawPower(g2, p2, POWER_P2, "PODER J2", "ENTER", true);
        drawFlag(g2, p1, FLAG_P1);
        drawFlag(g2, p2, FLAG_P2);
        drawScore(g2, p1, SCORE_P1);
        drawScore(g2, p2, SCORE_P2);
        drawTimer(g2);
        drawPauseButton(g2);
    }

    private void drawPower(Graphics2D g2, Player p, int[] r, String title, String key, boolean right) {
        drawBox(g2, r, p.hasAura() ? new Color(255, 245, 190) : BOX);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String status;
        if (p.hasAura()) {
            status = "AURA ACTIVA";
        } else if (p.isPowerReady()) {
            status = "¡LISTO! " + key;
        } else {
            status = Math.round(p.getPowerBar() * 100) + "%";
        }
        g2.drawString(title, r[0] + 6, r[1] + 14);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(status, r[0] + r[2] - 6 - fm.stringWidth(status), r[1] + 14);

        int bx = r[0] + 6;
        int by = r[1] + 20;
        int bw = r[2] - 12;
        int bh = 13;
        g2.setColor(BAR_BACK);
        g2.fillRoundRect(bx, by, bw, bh, 6, 6);
        int filled = (int) (bw * p.getPowerBar());
        g2.setColor(POWER);
        if (right) {
            g2.fillRoundRect(bx + bw - filled, by, filled, bh, 6, 6);
        } else {
            g2.fillRoundRect(bx, by, filled, bh, 6, 6);
        }

        Stats s = p.getStats();
        String line1 = "SPD " + s.getSpeed() + " · KCK " + s.getKick();
        String line2 = "JMP " + s.getJump() + " · SRV " + s.getSurvival() + " · " + p.getPoints() + " pts";
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        shadowText(g2, line1, r[0], r[1] + r[3] + 15, r[2], right);
        shadowText(g2, line2, r[0], r[1] + r[3] + 30, r[2], right);
    }

    private void drawFlag(Graphics2D g2, Player p, int[] r) {
        String country = p.getCharacter().getCountry();
        CharacterLook.drawFlag(g2, country, r[0], r[1], r[2], r[3]);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int tx = r[0] + (r[2] - fm.stringWidth(country)) / 2;
        labelText(g2, country, tx, r[1] + r[3] + 16);
    }

    private void drawScore(Graphics2D g2, Player p, int[] r) {
        drawBox(g2, r, BOX);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        centered(g2, String.valueOf(p.getGoals()), r[0], r[2], r[1] + 27);
    }

    private void drawTimer(Graphics2D g2) {
        Player p1 = game.getPlayer(1);
        Player p2 = game.getPlayer(2);
        boolean sudden = game.getRule() instanceof TimeRule
                && ((TimeRule) game.getRule()).isSuddenDeath(p1, p2);
        drawBox(g2, TIMER, BOX);
        g2.setColor(sudden ? new Color(200, 30, 30) : Color.BLACK);
        g2.setFont(new Font("Monospaced", Font.BOLD, sudden ? 22 : 34));
        centered(g2, game.getRule().getClockText(), TIMER[0], TIMER[2], TIMER[1] + 42);
        g2.setColor(new Color(80, 80, 90));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        centered(g2, game.getRule().getDescription(), TIMER[0], TIMER[2], TIMER[1] + 62);
    }

    private void drawPauseButton(Graphics2D g2) {
        int d = PAUSE_R * 2;
        g2.setColor(BOX);
        g2.fillOval(PAUSE_X - PAUSE_R, PAUSE_Y - PAUSE_R, d, d);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(PAUSE_X - PAUSE_R, PAUSE_Y - PAUSE_R, d, d);
        if (game.isPaused()) {
            int[] xs = {PAUSE_X - 6, PAUSE_X - 6, PAUSE_X + 9};
            int[] ys = {PAUSE_Y - 9, PAUSE_Y + 9, PAUSE_Y};
            g2.fillPolygon(xs, ys, 3);
        } else {
            g2.fillRect(PAUSE_X - 8, PAUSE_Y - 9, 5, 18);
            g2.fillRect(PAUSE_X + 3, PAUSE_Y - 9, 5, 18);
        }
    }
    // Jugadores y balones
    /**
     * Dibuja un jugador: con su sprite animado si está disponible,
     * o con el dibujo simple de respaldo si no.
     */
    private void drawPlayer(Graphics2D g2, Player p, int number) {
        double x = p.getX();
        double feet = p.getY();
        String tag = "J" + number;
        if (p.hasAura()) {
            g2.setColor(AURA);
            g2.fill(new Ellipse2D.Double(x - 60, feet - SPRITE_SIZE - 8, 120, SPRITE_SIZE + 16));
            g2.setColor(new Color(255, 200, 0, 200));
            g2.setStroke(new BasicStroke(3));
            g2.draw(new Ellipse2D.Double(x - 60, feet - SPRITE_SIZE - 8, 120, SPRITE_SIZE + 16));
        }
        BufferedImage frame = currentFrame(p, number);
        double top;
        if (frame != null) {
            drawSprite(g2, p, frame);
            top = feet - SPRITE_SIZE;
        } else {
            top = drawSimplePlayer(g2, p);
        }
        drawLife(g2, p, tag, x, top - 6);
        drawStatus(g2, p, x, top - 24);
    }

    /**
     * Cuadro de la animación que toca: caminando si se mueve en el suelo, quieto si no.
     * El reloj de la animación es el del partido, así se detiene en pausa.
     */
    private BufferedImage currentFrame(Player p, int number) {
        long time = (long) (game.getRule().getElapsed() * 1000);
        boolean walking = p.isOnGround() && Math.abs(p.getVx()) > 1;
        if (number == 1) {
            return walking ? Sprites.frameAt(Sprites.PLAYER1_WALK, time, Sprites.WALK_FRAME_MS)
                           : Sprites.frameAt(Sprites.PLAYER1_IDLE, time, Sprites.IDLE_FRAME_MS);
        }
        return walking ? Sprites.frameAt(Sprites.PLAYER2_WALK, time, Sprites.WALK_FRAME_MS)
                       : Sprites.frameAt(Sprites.PLAYER2_IDLE, time, Sprites.IDLE_FRAME_MS);
    }

    /**
     * Dibuja el sprite mirando hacia el arco rival,
     * semitransparente si está aturdido, y el pie extendido al patear.
     */
    private void drawSprite(Graphics2D g2, Player p, BufferedImage frame) {
        int dir = p.getAttackDirection();
        int left = (int) Math.round(p.getX() - SPRITE_SIZE / 2.0);
        int top = (int) Math.round(p.getY() - SPRITE_SIZE);
        Composite old = g2.getComposite();
        if (p.isStunned()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        }
        if (dir > 0) {
            g2.drawImage(frame, left, top, SPRITE_SIZE, SPRITE_SIZE, null);
        } else {
            g2.drawImage(frame, left + SPRITE_SIZE, top, -SPRITE_SIZE, SPRITE_SIZE, null);
        }
        g2.setComposite(old);
        BufferedImage foot = Sprites.get(Sprites.FOOT);
        if (p.isKicking() && foot != null) {
            AffineTransform saved = g2.getTransform();
            g2.translate(p.getX() + dir * (SPRITE_SIZE * 0.38), p.getY() - 14);
            g2.rotate(-dir * Math.toRadians(25));
            if (dir > 0) {
                g2.drawImage(foot, -FOOT_WIDTH / 2, -FOOT_HEIGHT / 2, FOOT_WIDTH, FOOT_HEIGHT, null);
            } else {
                g2.drawImage(foot, FOOT_WIDTH / 2, -FOOT_HEIGHT / 2, -FOOT_WIDTH, FOOT_HEIGHT, null);
            }
            g2.setTransform(saved);
        }
    }

    /**
     * Dibujo simple de respaldo (cuando no se encuentran las imágenes).
     * @return la altura (y) de la parte de arriba de la cabeza
     */
    private double drawSimplePlayer(Graphics2D g2, Player p) {
        String country = p.getCharacter().getCountry();
        double x = p.getX();
        double feet = p.getY();
        double bodyTop = feet - 45;
        double headR = 32;
        double headCy = bodyTop - headR + 8;
        int dir = p.getAttackDirection();

        g2.setColor(CharacterLook.shirt(country));
        g2.fill(new RoundRectangle2D.Double(x - 18, bodyTop, 36, 28, 10, 10));
        g2.setColor(CharacterLook.detail(country));
        g2.fill(new RoundRectangle2D.Double(x - 18, bodyTop + 24, 36, 12, 6, 6));

        g2.setColor(CharacterLook.skin(country));
        g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double hipY = bodyTop + 34;
        g2.drawLine((int) (x - dir * 8), (int) hipY, (int) (x - dir * 10), (int) feet - 4);
        if (p.isKicking()) {
            g2.drawLine((int) (x + dir * 8), (int) hipY, (int) (x + dir * 45), (int) feet - 18);
        } else {
            g2.drawLine((int) (x + dir * 8), (int) hipY, (int) (x + dir * 10), (int) feet - 4);
        }

        Color head = p.isStunned() ? CharacterLook.skin(country).darker() : CharacterLook.skin(country);
        g2.setColor(head);
        g2.fill(new Ellipse2D.Double(x - headR, headCy - headR, headR * 2, headR * 2));
        g2.setColor(CharacterLook.detail(country));
        g2.setStroke(new BasicStroke(3));
        g2.draw(new Ellipse2D.Double(x - headR, headCy - headR, headR * 2, headR * 2));
        g2.setColor(CharacterLook.shirt(country));
        g2.fill(new RoundRectangle2D.Double(x - headR + 2, headCy - 20, headR * 2 - 4, 8, 6, 6));
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(x + dir * 12 - 7, headCy - 8, 14, 14));
        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Double(x + dir * 15 - 3, headCy - 4, 7, 7));
        return headCy - headR;
    }

    /**
     * Creación el panel de la bandera del país, barra de vida y etiqueta del jugador.
     */
    private void drawLife(Graphics2D g2, Player p, String tag, double x, double y) {
        int w = 64;
        int h = 8;
        int left = (int) (x - w / 2.0);
        CharacterLook.drawFlag(g2, p.getCharacter().getCountry(), left - MINI_FLAG_WIDTH - 6,
                (int) y - 3, MINI_FLAG_WIDTH, MINI_FLAG_HEIGHT);
        double ratio = p.getLife() / p.getMaxLife();
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(left - 1, (int) y - 1, w + 2, h + 2, 6, 6);
        g2.setColor(ratio > 0.3 ? LIFE : LIFE_LOW);
        g2.fillRoundRect(left, (int) y, (int) (w * ratio), h, 6, 6);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String text = tag + " · " + Math.round(p.getLife());
        labelText(g2, text, left + w + 6, (int) y + 8);
    }

    /**
     * Etiqueta del efecto o del aturdimiento sobre el jugador.
     */
    private void drawStatus(Graphics2D g2, Player p, double x, double y) {
        String text = null;
        Color color = null;
        if (p.isStunned()) {
            text = "★ ATURDIDO " + String.format("%.1f", p.getStunTime()) + "s ★";
            color = Color.ORANGE;
        } else if (p.getEffect() != null) {
            Effect e = p.getEffect();
            text = e.getName() + " " + String.format("%.1f", e.getRemaining()) + "s";
            color = CharacterLook.effect(e.getName());
        }
        if (text == null) {
            return;
        }
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect((int) (x - w / 2.0 - 5), (int) (y - fm.getAscent()), w + 10, fm.getHeight(), 8, 8);
        g2.setColor(color);
        g2.drawString(text, (int) (x - w / 2.0), (int) y);
    }

    /**
     * Dibuja un balón real o falso. Gira según su posición,
     * como si rodara, y brilla con el color del tiro de poder si lleva uno.
     */
    private void drawBall(Graphics2D g2, Ball ball) {
        double r = Ball.RADIUS;
        double x = ball.getX();
        double y = ball.getY();
        if (ball.hasShot()) {
            Color glow = CharacterLook.shot(ball.getShot().getName());
            if (glow != null) {
                g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 90));
                g2.fill(new Ellipse2D.Double(x - r * 2, y - r * 2, r * 4, r * 4));
                g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 160));
                g2.fill(new Ellipse2D.Double(x - r * 1.4, y - r * 1.4, r * 2.8, r * 2.8));
            }
        }
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(x / r);
        BufferedImage image = Sprites.get(Sprites.BALL);
        if (image != null) {
            int size = (int) Math.round(r * 2);
            g2.drawImage(image, -size / 2, -size / 2, size, size, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(-r, -r, r * 2, r * 2));
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.draw(new Ellipse2D.Double(-r, -r, r * 2, r * 2));
            g2.fill(new Ellipse2D.Double(-5, -5, 10, 10));
            g2.fill(new Ellipse2D.Double(-r + 1, -4, 6, 8));
            g2.fill(new Ellipse2D.Double(r - 7, -4, 6, 8));
        }
        g2.setTransform(old);
    }
    // Menú de pausa
    private void drawPauseMenu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 100, (int) Field.WIDTH, (int) Field.HEIGHT - 100);
        drawPauseTitle(g2, "PAUSA", 215);
        drawMenuButton(g2, RESUME_BUTTON, "Continuar");
        drawMenuButton(g2, OPTIONS_BUTTON, "Opciones");
        drawMenuButton(g2, FINISH_BUTTON, "Terminar partido");
    }

    /**
     * Título amarillo con contorno negro, como "DOPO SOCCER" en el inicio.
     */
    private static void drawPauseTitle(Graphics2D g2, String text, int y) {
        Font font = new Font("SansSerif", Font.BOLD, 58);
        java.awt.Shape shape = font.createGlyphVector(g2.getFontRenderContext(), text)
                .getOutline(0, 0);
        double x = (Field.WIDTH - shape.getBounds2D().getWidth()) / 2;
        shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(shape);
        g2.setColor(GameStyle.GOLD);
        g2.fill(shape);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(shape);
    }

    private void drawMenuButton(Graphics2D g2, int[] r, String text) {
        drawBox(g2, r, Color.WHITE);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        centered(g2, text, r[0], r[2], r[1] + 32);
    }
    // Utilidades de dibujo
    /**
     * Recuadro blanco con borde negro grueso.
     */
    private static void drawBox(Graphics2D g2, int[] r, Color fill) {
        g2.setColor(fill);
        g2.fillRect(r[0], r[1], r[2], r[3]);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(r[0], r[1], r[2], r[3]);
    }

    private static void centered(Graphics2D g2, String text, int x, int width, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, x + (width - fm.stringWidth(text)) / 2, y);
    }

    /**
     * Texto blanco alineado a la izquierda o a la derecha del recuadro.
     */
    private static void shadowText(Graphics2D g2, String text, int x, int y, int width, boolean right) {
        FontMetrics fm = g2.getFontMetrics();
        int tx = right ? x + width - fm.stringWidth(text) : x;
        labelText(g2, text, tx, y);
    }

    /**
     * Texto blanco sobre un fondo oscuro translúcido, para que se lea en
     * cualquier escenario (claro u oscuro).
     */
    private static void labelText(Graphics2D g2, String text, int x, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(LABEL_BACKGROUND);
        g2.fillRoundRect(x - 4, y - fm.getAscent(), fm.stringWidth(text) + 8, fm.getHeight(), 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private static boolean inside(double x, double y, int[] r) {
        return x >= r[0] && x <= r[0] + r[2] && y >= r[1] && y <= r[1] + r[3];
    }
}
