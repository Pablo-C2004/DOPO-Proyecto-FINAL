package presentation;

// Clase principal del dominio: el partido
import domain.DOPOSoccer;
// Excepción propia del juego con sus mensajes de error
import domain.DOPOSoccerException;
// Registro de errores para los programadores
import domain.Log;

// Selector de archivos para abrir y salvar
import javax.swing.JFileChooser;
// filtro para ver solo archivos .dopo
import javax.swing.filechooser.FileNameExtensionFilter;
// evento al abrir o cerrar el menú
import javax.swing.event.MenuEvent;
// escucha cuando se abre o se cierra el menú
import javax.swing.event.MenuListener;
// archivo para guardar o abrir
import java.io.File;
// Ventana principal
import javax.swing.JFrame;
// Menú de la barra de menús
import javax.swing.JMenu;
// Color del texto del menú
import java.awt.Color;
// Barra de menús de la ventana
import javax.swing.JMenuBar;
// Opción dentro de un menú
import javax.swing.JMenuItem;
// Cuadros de mensaje y de confirmación
// Panel contenedor
import javax.swing.JPanel;
// Acción base para asociar a una tecla
import javax.swing.AbstractAction;
// Componente base de Swing (constantes de foco)
import javax.swing.JComponent;
// Combinación de tecla (atajo F11)
import javax.swing.KeyStroke;
// Utilidades de Swing (ejecutar en el hilo de la interfaz)
import javax.swing.SwingUtilities;
// Distribución que muestra una pantalla a la vez (inicio / juego)
import java.awt.CardLayout;
// Tamaño (ancho y alto) de un componente
import java.awt.Dimension;
// Entorno gráfico (área útil del monitor)
import java.awt.GraphicsEnvironment;
// Rectángulo (área del monitor)
import java.awt.Rectangle;
// Evento generado por una acción (tecla)
import java.awt.event.ActionEvent;
// Códigos de las teclas del teclado
import java.awt.event.KeyEvent;
// Adaptador para escuchar eventos de la ventana
import java.awt.event.WindowAdapter;
// Evento de la ventana (por ejemplo, cerrarla)
import java.awt.event.WindowEvent;

/**
 * Ventana principal de DOPOSoccer. Alterna entre la pantalla de inicio
 * y la pantalla del partido.
 * La ventana se puede agrandar o achicar arrastrando el borde, escoger un
 * tamaño en Options o pasar a pantalla completa (F11); el juego se ajusta solo.
 * Options es la misma ventana en el inicio (botón Options) y en el partido
 * (botón Options del menú de pausa).
 * Mientras se usa el menú Archivo el partido queda en pausa.
 */
public class DOPOSoccerGUI extends JFrame {

    // Nombre de la tarjeta de la pantalla de inicio
    private static final String START = "start";
    // Nombre de la tarjeta de juego
    private static final String GAME = "game";

    // Juego (dominio)
    private DOPOSoccer game;
    // Indica si el menú Archivo pausó el partido
    private boolean pausedByMenu;
    // Indica si hay una opción del menú Archivo en uso
    private boolean fileActionInProgress;
    // Resolución escogida en Options (por defecto media)
    private Resolution resolution;
    // Escenario (fondo) escogido en Options (por defecto pradera)
    private Stage stage;
    // Distribución que alterna entre pantallas
    private CardLayout cards;
    // Panel que contiene las pantallas
    private JPanel container;
    // Pantalla de inicio
    private StartPanel startPanel;
    // Pantalla del partido actual (null si no hay)
    private GameScreen gameScreen;

    // Opción de menú Nuevo
    private JMenuItem newItem;
    // Opción de menú Abrir
    private JMenuItem openItem;
    // Opción de menú Salvar
    private JMenuItem saveItem;
    // Opción de menú Salir
    private JMenuItem exitItem;
    // Último tamaño en ventana, para volver a él al salir de pantalla completa
    private Resolution windowedResolution;

    // Tamaño mínimo de la ventana cuando se arrastra el borde
    private static final Dimension MINIMUM_SIZE = new Dimension(440, 300);

    public DOPOSoccerGUI() {
        game = new DOPOSoccer();
        resolution = Resolution.MEDIUM;
        windowedResolution = Resolution.MEDIUM;
        stage = Stage.MEADOW;
        Sprites.load();
        prepareElements();
        prepareActions();
    }

    /**
     * Si faltan imágenes, avisa cuáles y que se usarán los dibujos simples.
     */
    private void warnMissingImages() {
        if (Sprites.getMissing().isEmpty()) {
            return;
        }
        GameStyle.message(this,
                "No se encontraron o no se pudieron leer estas imágenes en la carpeta '"
                        + Sprites.FOLDER + "':\n" + String.join(", ", Sprites.getMissing())
                        + "\n\nEl juego usará los dibujos simples en su lugar. El detalle quedó en el log.",
                "Imágenes faltantes");
    }

    private void prepareElements() {
        // Colores del juego para el menú Archivo (antes de crearlo)
        GameStyle.install();
        setTitle("DOPOSoccer");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setMinimumSize(MINIMUM_SIZE);
        prepareMenu();
        cards = new CardLayout();
        container = new JPanel(cards);
        startPanel = new StartPanel(this);
        container.add(startPanel, START);
        setContentPane(container);
        applyResolution();
    }

    private void prepareMenu() {
        JMenuBar bar = GameStyle.menuBar();
        JMenu file = new JMenu("Archivo");
        file.setForeground(Color.WHITE);
        newItem = new JMenuItem("Nuevo");
        openItem = new JMenuItem("Abrir...");
        saveItem = new JMenuItem("Salvar...");
        exitItem = new JMenuItem("Salir");
        file.add(newItem);
        file.addSeparator();
        file.add(openItem);
        file.add(saveItem);
        file.addSeparator();
        file.add(exitItem);
        bar.add(file);
        setJMenuBar(bar);
        file.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                pauseForMenu();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                resumeAfterMenuLater();
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                resumeAfterMenuLater();
            }
        });
    }

    private void prepareActions() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                whilePaused(DOPOSoccerGUI.this::optionExit);
            }
        });
        newItem.addActionListener(e -> whilePaused(this::optionNew));
        openItem.addActionListener(e -> whilePaused(this::optionOpen));
        saveItem.addActionListener(e -> whilePaused(this::optionSave));
        exitItem.addActionListener(e -> whilePaused(this::optionExit));
        // F11: entrar o salir de pantalla completa (funciona en el inicio y en el partido)
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "fullScreen");
        getRootPane().getActionMap().put("fullScreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullScreen();
            }
        });
    }
    // Botones de la pantalla de inicio 
    /**
     * Single Player jugador vs máquina pide el perfil de la máquina.
     */
    void optionSinglePlayer() {
        configureAndStart(DOPOSoccer.PLAYER_VS_MACHINE);
    }

    /**
     * Multiplayer jugador vs jugador en el mismo teclado.
     */
    void optionMultiplayer() {
        configureAndStart(DOPOSoccer.PLAYER_VS_PLAYER);
    }

    /**
     * Abre la ventana de Options. Si hay un partido, lo pausa mientras tanto.
     * El tamaño escogido se aplica al cerrar el diálogo.
     */
    void showOptions() {
        if (gameScreen != null) {
            gameScreen.pause();
        }
        Resolution before = resolution;
        new OptionsDialog(this, resolution, stage, game.isRunning()).setVisible(true);
        if (resolution != before) {
            applyResolution();
        }
    }

    /**
     * Guarda el tamaño de pantalla escogido (se aplica con applyResolution).
     */
    void setResolution(Resolution resolution) {
        this.resolution = resolution;
        if (!resolution.isFullScreen()) {
            windowedResolution = resolution;
        }
    }

    /**
     * Cambia el escenario y redibuja la pantalla de inicio y el partido.
     */
    void setStage(Stage stage) {
        this.stage = stage;
        startPanel.repaint();
        if (gameScreen != null) {
            gameScreen.setStage(stage);
        }
    }

    /**
     * F11: entra o sale de pantalla completa.
     */
    private void toggleFullScreen() {
        if (gameScreen != null) {
            gameScreen.pause();
        }
        setResolution(resolution.isFullScreen() ? windowedResolution : Resolution.FULL_SCREEN);
        applyResolution();
    }

    /**
     * Ajusta la ventana a la resolución actual. En pantalla completa quita
     * los bordes y ocupa todo el monitor; si no, toma el tamaño escogido
     * (sin pasarse del monitor) y se centra.
     */
    private void applyResolution() {
        boolean visible = isVisible();
        boolean full = resolution.isFullScreen();
        if (isUndecorated() != full) {
            // Quitar o poner los bordes solo se puede con la ventana cerrada
            dispose();
            setUndecorated(full);
        }
        if (full) {
            setResizable(false);
            Rectangle screen = getGraphicsConfiguration().getBounds();
            setBounds(screen);
        } else {
            setResizable(true);
            setExtendedState(NORMAL);
            container.setPreferredSize(fitToScreen(resolution.getWidth(), resolution.getHeight()));
            pack();
            setLocationRelativeTo(null);
        }
        if (visible) {
            setVisible(true);
        }
        container.revalidate();
        container.repaint();
    }

    /**
     * Si el tamaño pedido no cabe en el monitor, lo reduce manteniendo la proporción 2:1.
     */
    private static Dimension fitToScreen(int width, int height) {
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        // Espacio para la barra de título, la barra de menú y los bordes
        int maxWidth = usable.width - 20;
        int maxHeight = usable.height - 80;
        double factor = Math.min(1.0, Math.min((double) maxWidth / width, (double) maxHeight / height));
        return new Dimension((int) (width * factor), (int) (height * factor));
    }

    Stage getStage() {
        return stage;
    }

    void showStatsDialog() {
        if (game.isRunning()) {
            showError(DOPOSoccerException.MATCH_IN_PROGRESS);
            return;
        }
        new StatsDialog(this, game).setVisible(true);
    }
    // Navegación entre pantallas 
    /**
     * Pide la configuración del partido y lo inicia.
     */
    private void configureAndStart(String mode) {
        MatchSetupDialog setup = new MatchSetupDialog(this, mode);
        setup.setVisible(true);
        if (!setup.isAccepted()) {
            return;
        }
        startMatch(setup.getMode(), setup.getCharacter1(), setup.getCharacter2(),
                setup.getMatchType(), setup.getValue(), setup.getProfile());
    }

    private void startMatch(String mode, String country1, String country2, String matchType, int value, String machineProfile) {
        try {
            game.newMatch(mode, country1, country2, matchType, value, machineProfile);
            showGame();
        } catch (DOPOSoccerException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            Log.record(e);
            showError("Ocurrió un error inesperado al iniciar el partido. El detalle quedó en el log.");
        }
    }

    /**
     * Muestra la pantalla del partido.
     */
    private void showGame() {
        gameScreen = new GameScreen(this, game, stage);
        container.add(gameScreen, GAME);
        cards.show(container, GAME);
        container.revalidate();
        gameScreen.start();
    }

    /**
     * Vuelve a la pantalla de inicio.
     */
    void showStart() {
        if (gameScreen != null) {
            gameScreen.stop();
            container.remove(gameScreen);
            gameScreen = null;
        }
        cards.show(container, START);
        container.revalidate();
        container.repaint();
    }

    // ---- Pausa del menú Archivo ----

    /**
     * Pausa el partido al abrir el menú.
     */
    private void pauseForMenu() {
        if (gameScreen != null && game.isRunning() && !game.isPaused()) {
            gameScreen.pause();
            pausedByMenu = true;
        }
    }

    /**
     * Reanuda el partido solo si lo pausó el menú.
     */
    private void resumeAfterMenu() {
        if (pausedByMenu && gameScreen != null && game.isRunning() && game.isPaused()) {
            gameScreen.resume();
        }
        pausedByMenu = false;
    }

    /**
     * Reanuda al cerrar el menú, si no se escogió ninguna opción.
     */
    private void resumeAfterMenuLater() {
        SwingUtilities.invokeLater(() -> {
            if (!fileActionInProgress) {
                resumeAfterMenu();
            }
        });
    }

    /**
     * Ejecuta una opción del menú con el partido en pausa.
     */
    private void whilePaused(Runnable option) {
        fileActionInProgress = true;
        pauseForMenu();
        try {
            option.run();
        } finally {
            fileActionInProgress = false;
            resumeAfterMenu();
        }
    }

    // ---- Opciones del menú ----

    private void optionNew() {
        if (game.isRunning()) {
            boolean answer = GameStyle.confirm(this,
                    "¿Abandonar el partido actual? No se otorgarán puntos.",
                    "Nuevo partido");
            if (!answer) {
                return;
            }
            game.abandon();
        }
        showStart();
    }

    /**
     * Abre una partida guardada. Si tenía un partido, queda en pausa.
     */
    private void optionOpen() {
        JFileChooser chooser = fileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            DOPOSoccer loaded = DOPOSoccer.open(chooser.getSelectedFile());
            if (game.isRunning()) {
                game.abandon();
            }
            showStart();
            game = loaded;
            // la partida abierta no se reanuda sola
            pausedByMenu = false;
            if (game.isRunning()) {
                if (!game.isPaused()) {
                    game.togglePause();
                }
                showGame();
                GameStyle.message(this,
                        "Partida cargada (" + game.getPlayer(1).getGoals() + " - " + game.getPlayer(2).getGoals()
                                + "). Está en pausa: presiona P o ESC para continuar.",
                        "Abrir partida");
            } else {
                GameStyle.message(this,
                        "Se cargaron los jugadores con sus puntos y estadísticas.",
                        "Abrir partida");
            }
        } catch (DOPOSoccerException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            Log.record(e);
            showError("Ocurrió un error inesperado al abrir la partida. El detalle quedó en el log.");
        }
    }

    /**
     * Salva la partida actual en un archivo .dopo.
     */
    private void optionSave() {
        JFileChooser chooser = fileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = withExtension(chooser.getSelectedFile());
        if (file.exists() && !GameStyle.confirm(this,
                "El archivo " + file.getName() + " ya existe. ¿Desea reemplazarlo?", "Salvar partida")) {
            return;
        }
        try {
            game.save(file);
            GameStyle.message(this, "Partida guardada en " + file.getName() + ".", "Salvar partida");
        } catch (DOPOSoccerException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            Log.record(e);
            showError("Ocurrió un error inesperado al guardar la partida. El detalle quedó en el log.");
        }
    }

    /**
     * Selector que muestra solo las partidas .dopo.
     */
    private static JFileChooser fileChooser() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Partidas de DOPOSoccer (*." + DOPOSoccer.FILE_EXTENSION + ")", DOPOSoccer.FILE_EXTENSION));
        return chooser;
    }

    /**
     * Agrega .dopo si no se escribió.
     */
    private static File withExtension(File file) {
        String suffix = "." + DOPOSoccer.FILE_EXTENSION;
        if (file.getName().toLowerCase().endsWith(suffix)) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + suffix);
    }

    private void optionExit() {
        if (GameStyle.confirm(this, "¿Desea salir de DOPOSoccer?", "Salir")) {
            System.exit(0);
        }
    }

    private void showError(String message) {
        GameStyle.message(this, message, "DOPOSoccer");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DOPOSoccerGUI gui = new DOPOSoccerGUI();
            gui.setVisible(true);
            gui.warnMissingImages();
        });
    }
}
