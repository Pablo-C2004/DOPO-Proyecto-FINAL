package presentation;

// Clase principal del dominio: el partido
import domain.DOPOSoccer;
// Registro de errores para los programadores
import domain.Log;

// Acción base para asociar a una tecla
import javax.swing.AbstractAction;
// Mapa de nombre de acción a la acción que se ejecuta
import javax.swing.ActionMap;
// Mapa de tecla a nombre de acción
import javax.swing.InputMap;
// Componente base de Swing (constantes de foco)
import javax.swing.JComponent;
// Panel contenedor / lienzo de dibujo
import javax.swing.JPanel;
// Combinación de tecla presionada o soltada
import javax.swing.KeyStroke;
// Temporizador que mueve el juego en cada cuadro
import javax.swing.Timer;
// Distribución en zonas norte, sur, centro, este y oeste
import java.awt.BorderLayout;
// Evento generado por una acción (tecla o botón)
import java.awt.event.ActionEvent;
// Códigos de las teclas del teclado
import java.awt.event.KeyEvent;
// Adaptador para escuchar los clics del ratón
import java.awt.event.MouseAdapter;
// Evento de un clic del ratón
import java.awt.event.MouseEvent;
// Conjunto sin repetidos basado en tabla hash
import java.util.HashSet;
// Interfaz de conjunto sin elementos repetidos
import java.util.Set;

/**
 * Controla la pantalla del partido: lee el teclado (los dos jugadores en el
 * mismo teclado) y los clics, y mueve el reloj del juego.
 *
 * Controles:
 *   Jugador 1: A / D moverse, W saltar, S patear, E poder
 *   Jugador 2: ← / → moverse, ↑ saltar, ↓ patear, ENTER poder
 *   P o ESC pausar / continuar (también el botón redondo de pausa)
 *   En el menú de pausa: Continuar, Options (la misma ventana del inicio) y Terminar partido
 */
public class GameScreen extends JPanel {

    // Milisegundos entre cuadros (~60 por segundo)
    public static final int FRAME_MS = 16;
    // Paso de tiempo máximo por cuadro
    public static final double MAX_DT = 0.05;

    // Ventana principal
    private final DOPOSoccerGUI gui;
    // Juego (dominio)
    private final DOPOSoccer game;
    // Panel que dibuja el partido
    private final MatchPanel matchPanel;
    // Teclas que están presionadas en este momento
    private final Set<Integer> pressed;
    // Temporizador que avanza el juego
    private final Timer timer;
    // Instante (ns) del último cuadro
    private long lastTick;

    public GameScreen(DOPOSoccerGUI gui, DOPOSoccer game, Stage stage) {
        this.gui = gui;
        this.game = game;
        this.pressed = new HashSet<>();
        this.matchPanel = new MatchPanel(game, stage);
        this.timer = new Timer(FRAME_MS, e -> tick());
        prepareElements();
        prepareActions();
    }

    private void prepareElements() {
        setLayout(new BorderLayout());
        add(matchPanel, BorderLayout.CENTER);
    }

    private void prepareActions() {
        matchPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                optionClick(e.getX(), e.getY());
            }
        });
        prepareKeys();
    }

    /**
     * Atajos de teclado.
     */
    private void prepareKeys() {
        int[] held = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W,
                      KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP};
        for (int key : held) {
            bind(key, false, "press" + key, () -> pressed.add(key));
            bind(key, true, "release" + key, () -> pressed.remove(key));
        }
        bind(KeyEvent.VK_S, false, "kick1", () -> game.kick(1));
        bind(KeyEvent.VK_E, false, "power1", () -> game.activatePower(1));
        if (!game.isVsMachine()) {
            bind(KeyEvent.VK_DOWN, false, "kick2", () -> game.kick(2));
            bind(KeyEvent.VK_ENTER, false, "power2", () -> game.activatePower(2));
        }
        bind(KeyEvent.VK_P, false, "pause", this::optionPause);
        bind(KeyEvent.VK_ESCAPE, false, "pauseEscape", this::optionPause);
    }

    private void bind(int key, boolean onRelease, String name, Runnable action) {
        InputMap input = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();
        input.put(KeyStroke.getKeyStroke(key, 0, onRelease), name);
        actions.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    /**
     * Cambia el escenario del partido en curso (desde Options).
     */
    public void setStage(Stage stage) {
        matchPanel.setStage(stage);
    }

    /**
     * Pausa el partido si está corriendo (por ejemplo, al abrir Options).
     */
    public void pause() {
        if (game.isRunning() && !game.isPaused()) {
            optionPause();
        }
    }

    /**
     * Continúa el partido si está en pausa.
     */
    public void resume() {
        if (game.isRunning() && game.isPaused()) {
            optionPause();
        }
    }

    public void start() {
        lastTick = System.nanoTime();
        timer.start();
    }

    public void stop() {
        timer.stop();
        pressed.clear();
    }

    /**
     * Un cuadro del juego lee las teclas lee lo que hace domain y redibuja
     */
    private void tick() {
        try {
            long now = System.nanoTime();
            double dt = Math.min(MAX_DT, (now - lastTick) / 1e9);
            lastTick = now;
            applyHeldKeys();
            game.update(dt);
            matchPanel.repaint();
            if (game.isFinished()) {
                stop();
                GameStyle.message(gui, game.getResultMessage(), "Fin del partido");
                gui.showStart();
            }
        } catch (Exception e) {
            stop();
            Log.record(e);
            GameStyle.message(gui,
                    "Ocurrió un error inesperado durante el partido. El detalle quedó en el log.",
                    "Error");
            gui.showStart();
        }
    }
    
    private void applyHeldKeys() {
        game.setDirection(1, axis(KeyEvent.VK_A, KeyEvent.VK_D));
        if (pressed.contains(KeyEvent.VK_W)) {
            game.jump(1);
        }
        if (!game.isVsMachine()) {
            game.setDirection(2, axis(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT));
            if (pressed.contains(KeyEvent.VK_UP)) {
                game.jump(2);
            }
        }
    }

    private int axis(int leftKey, int rightKey) {
        int dir = 0;
        if (pressed.contains(leftKey)) {
            dir--;
        }
        if (pressed.contains(rightKey)) {
            dir++;
        }
        return dir;
    }

    /**
     * permite utiliar el mosue para dar clic sobre el botón de pausa o sobre el menú de pausa.
     */
    private void optionClick(int x, int y) {
        String action = matchPanel.actionAt(x, y);
        if (MatchPanel.PAUSE.equals(action) || MatchPanel.RESUME.equals(action)) {
            optionPause();
        } else if (MatchPanel.OPTIONS.equals(action)) {
            gui.showOptions();
            lastTick = System.nanoTime();
        } else if (MatchPanel.FINISH.equals(action)) {
            optionFinish();
        }
    }

    private void optionPause() {
        game.togglePause();
        pressed.clear();
        lastTick = System.nanoTime();
        matchPanel.repaint();
    }

    private void optionFinish() {
        if (GameStyle.confirm(gui,
                "¿Terminar el partido ahora? Gana quien vaya adelante en el marcador.",
                "Terminar partido")) {
            game.finish();
        }
        lastTick = System.nanoTime();
    }
}
