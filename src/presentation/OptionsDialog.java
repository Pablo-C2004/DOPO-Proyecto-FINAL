package presentation;

// Botón
import javax.swing.JButton;
// Lista desplegable de opciones
import javax.swing.JComboBox;
// Ventana de diálogo
import javax.swing.JDialog;
// Etiqueta de texto
import javax.swing.JLabel;
// Panel contenedor
import javax.swing.JPanel;
// Distribución en zonas norte, sur, centro, este y oeste
import java.awt.BorderLayout;
// Distribución en cuadrícula de celdas iguales
import java.awt.GridLayout;

/**
 * Ventana de Opciones (settings): tamaño de la pantalla, escenario, mejora de
 * estadísticas y los controles del teclado. Se puede abrir desde la pantalla
 * de inicio o desde el menú Configuración, incluso durante un partido
 * (el partido se pausa mientras está abierta).
 * "Aplicar" cambia la ventana sin cerrar el diálogo; "Aceptar" aplica y cierra.
 */
public class OptionsDialog extends JDialog {

    // Ventana principal (aplica los cambios y abre las estadísticas)
    private final DOPOSoccerGUI gui;
    // Selector del tamaño de la pantalla
    private JComboBox<Resolution> resolutionBox;
    // Selector del escenario (fondo) de la cancha
    private JComboBox<Stage> stageBox;
    // Botón para abrir la mejora de estadísticas
    private JButton statsButton;
    // Botón para aplicar sin cerrar
    private JButton applyButton;
    // Botón para aplicar y cerrar
    private JButton acceptButton;
    // Botón para cerrar sin cambiar nada
    private JButton cancelButton;

    public OptionsDialog(DOPOSoccerGUI gui, Resolution current, Stage currentStage, boolean matchRunning) {
        super(gui, "Opciones", true);
        this.gui = gui;
        prepareElements(current, currentStage, matchRunning);
        prepareActions();
        pack();
        setResizable(false);
        setLocationRelativeTo(gui);
    }

    private void prepareElements(Resolution current, Stage currentStage, boolean matchRunning) {
        resolutionBox = GameStyle.comboBox(Resolution.values());
        resolutionBox.setSelectedItem(current);
        stageBox = GameStyle.comboBox(Stage.values());
        stageBox.setSelectedItem(currentStage);
        statsButton = GameStyle.button("Mejorar estadísticas");
        // Las estadísticas solo se mejoran fuera de un partido
        statsButton.setEnabled(!matchRunning);
        applyButton = GameStyle.button("Aplicar");
        acceptButton = GameStyle.primaryButton("Aceptar");
        cancelButton = GameStyle.button("Cancelar");

        JPanel form = GameStyle.card(new GridLayout(3, 2, 14, 10));
        form.add(GameStyle.label("Tamaño de la pantalla"));
        form.add(resolutionBox);
        form.add(GameStyle.label("Escenario"));
        form.add(stageBox);
        form.add(GameStyle.label("Estadísticas de los jugadores"));
        form.add(statsButton);

        JPanel controls = GameStyle.card(new BorderLayout(0, 6));
        controls.add(GameStyle.title("Controles", 20), BorderLayout.NORTH);
        JLabel keys = GameStyle.label("<html>"
                + "Jugador 1: A / D mover · W saltar · S patear · E poder<br>"
                + "Jugador 2: ← / → mover · ↑ saltar · ↓ patear · ENTER poder<br>"
                + "P o ESC: pausa · F11: pantalla completa<br>"
                + "También puedes cambiar el tamaño arrastrando el borde de la ventana.</html>");
        keys.setFont(GameStyle.font(13));
        controls.add(keys, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(controls, BorderLayout.CENTER);

        GameStyle.decorate(this, getTitle(), center,
                GameStyle.buttonRow(applyButton, cancelButton, acceptButton));
        getRootPane().setDefaultButton(acceptButton);
    }

    private void prepareActions() {
        statsButton.addActionListener(e -> gui.showStatsDialog());
        applyButton.addActionListener(e -> {
            apply();
            setLocationRelativeTo(gui);
        });
        acceptButton.addActionListener(e -> {
            apply();
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());
    }

    /**
     * Envía a la ventana principal el tamaño y el escenario escogidos.
     */
    private void apply() {
        gui.setStage((Stage) stageBox.getSelectedItem());
        gui.setResolution((Resolution) resolutionBox.getSelectedItem());
    }
}
