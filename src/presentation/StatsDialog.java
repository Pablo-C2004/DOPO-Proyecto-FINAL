package presentation;

// Clase principal del dominio: el partido
import domain.DOPOSoccer;
// Excepción propia del juego con sus mensajes de error
import domain.DOPOSoccerException;
// Registro de errores para los programadores
import domain.Log;
// Jugador: estadísticas, puntos y estado en el partido
import domain.Player;
// Estadísticas del jugador (speed, kick, jump, survival)
import domain.Stats;

// Fábrica de bordes para los paneles
import javax.swing.BorderFactory;
// Botón
import javax.swing.JButton;
// Componente base (barra de nivel dibujada a mano)
import javax.swing.JComponent;
// Ventana de diálogo
import javax.swing.JDialog;
// Ventana principal
import javax.swing.JFrame;
// Etiqueta de texto
import javax.swing.JLabel;
// Panel contenedor / lienzo de dibujo
import javax.swing.JPanel;
// Distribución en zonas norte, sur, centro, este y oeste
import java.awt.BorderLayout;
// Tipo de letra de los textos
import java.awt.Font;
// Contexto de dibujo básico
import java.awt.Graphics;
// Contexto de dibujo 2D
import java.awt.Graphics2D;
// Tamaño de un componente
import java.awt.Dimension;
// Opciones de calidad de dibujo
import java.awt.RenderingHints;
// Colores
import java.awt.Color;
// Distribución en cuadrícula de celdas iguales
import java.awt.GridLayout;

/**
 * Diálogo para mejorar las estadísticas de cada jugador con sus puntos.
 * Precio de cada punto: 500, 1000, 2000, ... se van duplicando.
 */
public class StatsDialog extends JDialog {

    // Juego (para mejorar estadísticas)
    private final DOPOSoccer game;
    // Panel con la información de los dos jugadores
    private final JPanel content;

    public StatsDialog(JFrame owner, DOPOSoccer game) {
        super(owner, "Mejorar estadísticas", true);
        this.game = game;
        this.content = new JPanel(new GridLayout(1, 2, 16, 0));
        content.setOpaque(false);
        JButton close = GameStyle.primaryButton("Cerrar");
        close.addActionListener(e -> dispose());
        GameStyle.decorate(this, getTitle(), content, GameStyle.buttonRow(close));
        refresh();
        pack();
        setLocationRelativeTo(owner);
    }

    private void refresh() {
        content.removeAll();
        content.add(playerPanel(1));
        content.add(playerPanel(2));
        content.revalidate();
        content.repaint();
        pack();
    }

    private JPanel playerPanel(int number) {
        Player p = game.getPlayer(number);
        JPanel card = GameStyle.card(new BorderLayout(0, 8));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(GameStyle.title(p.getName(), 20), BorderLayout.WEST);
        header.add(GameStyle.label(p.getPoints() + " puntos"), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel table = new JPanel(new GridLayout(Stats.NAMES.length + 1, 3, 10, 6));
        table.setOpaque(false);
        table.add(GameStyle.label("Estadística"));
        table.add(GameStyle.label("Nivel"));
        table.add(GameStyle.label("Mejorar"));
        for (String stat : Stats.NAMES) {
            try {
                Stats s = p.getStats();
                JLabel name = GameStyle.label(stat);
                name.setFont(GameStyle.font(14).deriveFont(Font.PLAIN));
                table.add(name);
                table.add(new LevelBar(s.get(stat), Stats.MAX));
                JButton button = GameStyle.button(s.isMax(stat) ? "Máximo" : "+1 (" + s.upgradeCost(stat) + ")");
                button.setFont(GameStyle.font(13));
                button.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                button.setEnabled(!s.isMax(stat));
                button.addActionListener(e -> upgrade(number, stat));
                table.add(button);
            } catch (DOPOSoccerException e) {
                Log.record(e);
            }
        }
        card.add(table, BorderLayout.CENTER);
        return card;
    }

    private void upgrade(int number, String stat) {
        try {
            game.upgradeStat(number, stat);
            refresh();
        } catch (DOPOSoccerException e) {
            GameStyle.message(this, e.getMessage(), "No se pudo mejorar");
        }
    }

    /**
     * Barra de nivel: un cuadrito por cada punto posible, rellenos en amarillo
     * hasta el nivel actual, con el número al lado.
     */
    private static class LevelBar extends JComponent {
        private final int level;
        private final int max;

        LevelBar(int level, int max) {
            this.level = level;
            this.max = max;
            setPreferredSize(new Dimension(Math.max(80, max * 12 + 34), 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = 10;
            int y = (getHeight() - size) / 2;
            int step = Math.max(size + 2, Math.min(14, (getWidth() - 34) / Math.max(1, max)));
            for (int i = 0; i < max; i++) {
                int x = i * step;
                g2.setColor(i < level ? GameStyle.GOLD : Color.WHITE);
                g2.fillRect(x, y, size, size);
                g2.setColor(GameStyle.INK);
                g2.drawRect(x, y, size, size);
            }
            g2.setFont(GameStyle.font(12));
            g2.drawString(level + "/" + max, max * step + 4, y + size);
            g2.dispose();
        }
    }
}
