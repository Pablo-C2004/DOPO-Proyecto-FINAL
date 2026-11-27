package presentation;

// Personaje Camerún (tiro Rayo)
import domain.Cameroon;
// Personaje Nigeria (tiro Tornado)
import domain.Nigeria;
// Personaje Corea del Sur (tiro Aura Azul)
import domain.SouthKorea;
// Personaje Estados Unidos (tiro Ilusión)
import domain.UnitedStates;

// Colores para dibujar y para los componentes
import java.awt.Color;
// Contexto de dibujo 2D avanzado (escala, antialias, figuras)
import java.awt.Graphics2D;
// Grosor y estilo de las líneas que se dibujan
import java.awt.BasicStroke;
// Polígono para dibujar la estrella de la bandera de Camerún
import java.awt.Polygon;

/**
 * Apariencia de cada personaje, su tiro y su efecto.
 * Claude dió los codigos para el dibujo de las banderas.
 */
public final class CharacterLook {

    private CharacterLook() {
    }

    /**
     * Color de la camiseta.
     */
    public static Color shirt(String country) {
        switch (country) {
            case SouthKorea.COUNTRY: return new Color(205, 30, 50);
            case Cameroon.COUNTRY: return new Color(0, 122, 61);
            case Nigeria.COUNTRY: return new Color(240, 240, 240);
            case UnitedStates.COUNTRY: return new Color(25, 45, 110);
            default: return Color.GRAY;
        }
    }

    /**
     * Color secundario (pantaloneta y detalles).
     */
    public static Color detail(String country) {
        switch (country) {
            case SouthKorea.COUNTRY: return new Color(0, 52, 120);
            case Cameroon.COUNTRY: return new Color(206, 17, 38);
            case Nigeria.COUNTRY: return new Color(0, 135, 81);
            case UnitedStates.COUNTRY: return new Color(190, 30, 45);
            default: return Color.DARK_GRAY;
        }
    }

    /**
     * Tono de piel.
     */
    public static Color skin(String country) {
        switch (country) {
            case SouthKorea.COUNTRY: return new Color(245, 205, 165);
            case Cameroon.COUNTRY: return new Color(120, 80, 50);
            case Nigeria.COUNTRY: return new Color(95, 62, 40);
            case UnitedStates.COUNTRY: return new Color(240, 195, 150);
            default: return new Color(220, 180, 140);
        }
    }

    /**
     * Color del brillo del balón cuando lleva un tiro de poder. Null = sin brillo.
     */
    public static Color shot(String shotName) {
        switch (shotName) {
            case "Aura Azul": return new Color(60, 140, 255);
            case "Rayo": return new Color(255, 230, 40);
            case "Tornado": return new Color(150, 110, 70);
            default: return null;
        }
    }

    /**
     * Color con el que se anuncia un efecto sobre el jugador.
     */
    public static Color effect(String effectName) {
        switch (effectName) {
            case "Empuje": return new Color(60, 140, 255);
            case "Electrocutado": return new Color(255, 215, 0);
            case "Inconsciente": return new Color(170, 120, 255);
            case "Confusión": return new Color(255, 120, 180);
            default: return Color.WHITE;
        }
    }

    /**
     * Dibuja la bandera del país del personaje dentro del rectángulo indicado.
     */
    public static void drawFlag(Graphics2D g2, String country, int x, int y, int w, int h) {
        switch (country) {
            case SouthKorea.COUNTRY: drawKorea(g2, x, y, w, h); break;
            case Cameroon.COUNTRY: drawCameroon(g2, x, y, w, h); break;
            case Nigeria.COUNTRY: drawNigeria(g2, x, y, w, h); break;
            case UnitedStates.COUNTRY: drawUnitedStates(g2, x, y, w, h); break;
            default:
                g2.setColor(Color.GRAY);
                g2.fillRect(x, y, w, h);
        }
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(x, y, w, h);
    }

    /**
     * Corea del Sur: fondo blanco, círculo rojo y azul, cuatro grupos de barras.
     */
    private static void drawKorea(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, w, h);
        int d = h / 2;
        int cx = x + w / 2;
        int cy = y + h / 2;
        g2.setColor(new Color(205, 46, 58));
        g2.fillArc(cx - d / 2, cy - d / 2, d, d, 0, 180);
        g2.setColor(new Color(0, 71, 160));
        g2.fillArc(cx - d / 2, cy - d / 2, d, d, 180, 180);
        g2.setColor(Color.BLACK);
        int bw = w / 7;
        int bh = 2;
        int[][] corners = {{x + 4, y + 5}, {x + w - 4 - bw, y + 5}, {x + 4, y + h - 13}, {x + w - 4 - bw, y + h - 13}};
        for (int[] c : corners) {
            for (int i = 0; i < 3; i++) {
                g2.fillRect(c[0], c[1] + i * 3, bw, bh);
            }
        }
    }

    /**
     * Camerún: verde, rojo y amarillo vertical con una estrella amarilla.
     */
    private static void drawCameroon(Graphics2D g2, int x, int y, int w, int h) {
        int third = w / 3;
        g2.setColor(new Color(0, 122, 61));
        g2.fillRect(x, y, third, h);
        g2.setColor(new Color(206, 17, 38));
        g2.fillRect(x + third, y, third, h);
        g2.setColor(new Color(252, 209, 22));
        g2.fillRect(x + 2 * third, y, w - 2 * third, h);
        g2.fill(star(x + w / 2, y + h / 2, h / 5.0));
    }

    /**
     * Nigeria: verde, blanco y verde vertical.
     */
    private static void drawNigeria(Graphics2D g2, int x, int y, int w, int h) {
        int third = w / 3;
        g2.setColor(new Color(0, 135, 81));
        g2.fillRect(x, y, w, h);
        g2.setColor(Color.WHITE);
        g2.fillRect(x + third, y, third, h);
    }

    /**
     * Estados Unidos: franjas rojas y blancas con el cuadro azul y estrellas.
     */
    private static void drawUnitedStates(Graphics2D g2, int x, int y, int w, int h) {
        double stripe = h / 13.0;
        for (int i = 0; i < 13; i++) {
            g2.setColor(i % 2 == 0 ? new Color(178, 34, 52) : Color.WHITE);
            g2.fillRect(x, (int) (y + i * stripe), w, (int) Math.ceil(stripe));
        }
        int cw = (int) (w * 0.42);
        int ch = (int) (stripe * 7);
        g2.setColor(new Color(60, 59, 110));
        g2.fillRect(x, y, cw, ch);
        g2.setColor(Color.WHITE);
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                g2.fillOval(x + 3 + col * (cw - 6) / 5, y + 3 + row * (ch - 6) / 4, 3, 3);
            }
        }
    }

    /**
     * Estrella de cinco puntas centrada en (cx, cy).
     */
    private static Polygon star(int cx, int cy, double r) {
        Polygon p = new Polygon();
        for (int i = 0; i < 10; i++) {
            double radius = i % 2 == 0 ? r : r * 0.4;
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            p.addPoint((int) Math.round(cx + radius * Math.cos(angle)), (int) Math.round(cy + radius * Math.sin(angle)));
        }
        return p;
    }
}
