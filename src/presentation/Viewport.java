package presentation;

// Medidas lógicas de la cancha (ancho, alto, suelo, arcos)
import domain.Field;

// Contexto de dibujo 2D avanzado (escala, antialias, figuras)
import java.awt.Graphics2D;

/**
 * Convierte entre la pantalla lógica del juego (1000 x 500) y el tamaño real
 * del panel. La imagen se estira o se encoge para ocupar todo el panel sin
 * deformarse ni recortarse; si sobra espacio, queda centrada.
 */
public final class Viewport {

    private Viewport() {
    }

    /**
     * Escala que hace caber la pantalla lógica en un panel de w x h píxeles.
     */
    public static double scale(int w, int h) {
        return Math.min(w / Field.WIDTH, h / Field.HEIGHT);
    }

    /**
     * Margen horizontal (en píxeles) para centrar la pantalla lógica.
     */
    public static double offsetX(int w, int h) {
        return (w - Field.WIDTH * scale(w, h)) / 2;
    }

    /**
     * Margen vertical (en píxeles) para centrar la pantalla lógica.
     */
    public static double offsetY(int w, int h) {
        return (h - Field.HEIGHT * scale(w, h)) / 2;
    }

    /**
     * Prepara el contexto de dibujo para dibujar en coordenadas lógicas.
     */
    public static void apply(Graphics2D g2, int w, int h) {
        g2.translate(offsetX(w, h), offsetY(w, h));
        double s = scale(w, h);
        g2.scale(s, s);
    }

    /**
     * Convierte una x de la pantalla real a coordenadas lógicas.
     */
    public static double toLogicalX(int px, int w, int h) {
        return (px - offsetX(w, h)) / scale(w, h);
    }

    /**
     * Convierte una y de la pantalla real a coordenadas lógicas.
     */
    public static double toLogicalY(int py, int w, int h) {
        return (py - offsetY(w, h)) / scale(w, h);
    }
}
