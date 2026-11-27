package presentation;

// Medidas lógicas de la cancha (ancho, alto, suelo, arcos)
import domain.Field;

// Grosor y estilo de las líneas que se dibujan
import java.awt.BasicStroke;
// Colores para dibujar y para los componentes
import java.awt.Color;
// Relleno con degradado de color (cielo)
import java.awt.GradientPaint;
// Contexto de dibujo 2D avanzado (escala, antialias, figuras)
import java.awt.Graphics2D;
// Opciones de calidad de dibujo 
import java.awt.RenderingHints;
// Imagen en memoria que se puede dibujar
import java.awt.image.BufferedImage;

/**
 * Fondo de la cancha según el escenario escogido (Pradera, Cueva o Desierto) y los arcos.
 * Si faltan las imágenes, dibuja un estadio simple (cielo, tribunas y césped).
 * Lo usan la pantalla de inicio y la pantalla del partido.
 * Dibuja en coordenadas lógicas de la cancha (1000 x 500).
 */
public final class Scenery {

    // Color de la parte alta del cielo
    private static final Color SKY_TOP = new Color(40, 70, 140);
    // Color de la parte baja del cielo
    private static final Color SKY_BOTTOM = new Color(110, 160, 220);
    // Color de las tribunas
    private static final Color STANDS = new Color(60, 60, 75);
    // Color del público en las tribunas
    private static final Color CROWD = new Color(85, 85, 105);
    // Color del césped
    private static final Color GRASS = new Color(60, 150, 60);
    // Color de las franjas oscuras del césped
    private static final Color GRASS_DARK = new Color(50, 130, 50);

    // En la imagen del arco, x (en píxeles) donde termina el travesaño
    private static final double GOAL_IMAGE_BAR_END_X = 68;
    // En la imagen del arco, y (en píxeles) del travesaño
    private static final double GOAL_IMAGE_BAR_Y = 20;
    // En la imagen del arco, y (en píxeles) del suelo
    private static final double GOAL_IMAGE_GROUND_Y = 240;

    private Scenery() {
    }

    /**
     * Dibuja el fondo del escenario y los dos arcos. Si la imagen del
     * escenario no está disponible, dibuja el estadio simple de respaldo.
     */
    public static void drawStadium(Graphics2D g2, Stage stage) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        BufferedImage background = Sprites.get(stage.getImage());
        if (background != null) {
            drawBackground(g2, background, stage.getGroundFraction());
        } else {
            drawSimpleStadium(g2);
        }
        drawGoals(g2);
    }

    /**
     * Dibuja la imagen de fondo a lo ancho de la cancha, alineando el suelo
     * de la imagen con el suelo del dominio (Field.GROUND) lo que sobra se recorta.
     */
    private static void drawBackground(Graphics2D g2, BufferedImage image, double groundFraction) {
        double width = Field.WIDTH;
        double height = width * image.getHeight() / image.getWidth();
        double top = Field.GROUND - groundFraction * height;
        if (top + height < Field.HEIGHT) {
            top = Field.HEIGHT - height;
        }
        g2.drawImage(image, 0, (int) Math.round(top), (int) Math.round(width), (int) Math.round(height), null);
    }

    /**
     * Estadio simple de respaldo.
     */
    private static void drawSimpleStadium(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 0, SKY_TOP, 0, (float) Field.CROSSBAR_Y, SKY_BOTTOM));
        g2.fillRect(0, 0, (int) Field.WIDTH, (int) Field.GROUND);
        g2.setColor(STANDS);
        g2.fillRect(0, 150, (int) Field.WIDTH, 120);
        g2.setColor(CROWD);
        for (int row = 0; row < 4; row++) {
            for (int x = 10 + (row % 2) * 12; x < Field.WIDTH; x += 24) {
                g2.fillOval(x, 160 + row * 28, 12, 12);
            }
        }
        drawGrass(g2);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(0, (int) Field.GROUND, (int) Field.WIDTH, (int) Field.GROUND);
        g2.drawLine((int) (Field.WIDTH / 2), (int) Field.GROUND, (int) (Field.WIDTH / 2), (int) Field.HEIGHT);
    }

    /**
     * Dibuja los dos arcos con sus imágenes o el dibujo de respaldo.
     */
    private static void drawGoals(Graphics2D g2) {
        BufferedImage left = Sprites.get(Sprites.GOAL_LEFT);
        BufferedImage right = Sprites.get(Sprites.GOAL_RIGHT);
        if (left != null && right != null) {
            drawGoalImage(g2, left, true);
            drawGoalImage(g2, right, false);
        } else {
            drawGoal(g2, 0);
            drawGoal(g2, Field.WIDTH - Field.GOAL_WIDTH);
        }
    }

    /**
     * Césped repite la imagen grass.png a lo largo de la cancha
     * o franjas verdes si la imagen no está.
     */
    private static void drawGrass(Graphics2D g2) {
        int top = (int) Field.GROUND;
        int height = (int) (Field.HEIGHT - Field.GROUND);
        BufferedImage grass = Sprites.get(Sprites.GRASS);
        if (grass != null) {
            int tileWidth = grass.getWidth() * height / grass.getHeight();
            for (int x = 0; x < Field.WIDTH; x += tileWidth) {
                g2.drawImage(grass, x, top, tileWidth, height, null);
            }
            return;
        }
        g2.setColor(GRASS);
        g2.fillRect(0, top, (int) Field.WIDTH, height);
        g2.setColor(GRASS_DARK);
        for (int x = 0; x < Field.WIDTH; x += 100) {
            g2.fillRect(x, top, 50, height);
        }
    }

    /**
     * Dibuja la imagen del arco escalada para que su travesaño coincida
     * con el travesaño del dominio.
     */
    private static void drawGoalImage(Graphics2D g2, BufferedImage image, boolean leftSide) {
        double scaleX = Field.GOAL_WIDTH / GOAL_IMAGE_BAR_END_X;
        double scaleY = (Field.GROUND - Field.CROSSBAR_Y) / (GOAL_IMAGE_GROUND_Y - GOAL_IMAGE_BAR_Y);
        int width = (int) Math.round(image.getWidth() * scaleX);
        int height = (int) Math.round(image.getHeight() * scaleY);
        int top = (int) Math.round(Field.CROSSBAR_Y - GOAL_IMAGE_BAR_Y * scaleY);
        int x = leftSide ? 0 : (int) Field.WIDTH - width;
        g2.drawImage(image, x, top, width, height, null);
    }

    /**
     * Dibuja un arco con su red, el travesaño y el poste respaldo sin imágenes.
     */
    private static void drawGoal(Graphics2D g2, double left) {
        int x = (int) left;
        int top = (int) Field.CROSSBAR_Y;
        int w = (int) Field.GOAL_WIDTH;
        int h = (int) (Field.GROUND - Field.CROSSBAR_Y);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRect(x, top, w, h);
        g2.setColor(new Color(255, 255, 255, 140));
        g2.setStroke(new BasicStroke(1));
        for (int i = x; i <= x + w; i += 10) {
            g2.drawLine(i, top, i, top + h);
        }
        for (int j = top; j <= top + h; j += 10) {
            g2.drawLine(x, j, x + w, j);
        }
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x, top, x + w, top);
        int post = left == 0 ? x + w : x;
        g2.drawLine(post, top, post, top + h);
    }
}
