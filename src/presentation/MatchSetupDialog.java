package presentation;

// Fábrica que crea personajes a partir del país
import domain.CharacterFactory;
// Clase principal del dominio: el partido
import domain.DOPOSoccer;
// Regla de partido por goles
import domain.GoalRule;
// Regla (condición de victoria) del partido
import domain.MatchRule;
// Regla de partido por tiempo (con muerte súbita)
import domain.TimeRule;

// Espacios fijos entre componentes
import javax.swing.Box;
// Agrupa botones de opción para que solo uno quede marcado
import javax.swing.ButtonGroup;
// campo texto interno de spinner
import javax.swing.JFormattedTextField;
// cuadro de mensaje para errores de formato
import javax.swing.JOptionPane;
// Botón
import javax.swing.JButton;
// Lista desplegable de opciones
import javax.swing.JComboBox;
// Ventana de diálogo
import javax.swing.JDialog;
// Ventana principal
import javax.swing.JFrame;
// Etiqueta de texto
import javax.swing.JLabel;
// Panel contenedor
import javax.swing.JPanel;
// Botón de opción (por tiempo / por goles)
import javax.swing.JRadioButton;
// Campo numérico con flechas para subir y bajar
import javax.swing.JSpinner;
// Modelo numérico (mínimo, máximo, paso) del spinner
import javax.swing.SpinnerNumberModel;
// Componente genérico de AWT (para agregar filas al formulario)
import java.awt.Component;
// Distribución de componentes en fila
import java.awt.FlowLayout;
// Restricciones de posición para GridBagLayout
import java.awt.GridBagConstraints;
// Distribución en cuadrícula flexible (formulario)
import java.awt.GridBagLayout;
// Márgenes alrededor de un componente
import java.awt.Insets;

/**
 * Configuración del partido que aparece al escoger Single Player o Multiplayer
 * perfil de la máquina, personajes, tipo de partido y su valor.
 */
public class MatchSetupDialog extends JDialog {

    // Modalidad escogida en la pantalla de inicio
    private final String mode;
    // Indica si el usuario presionó "Jugar"
    private boolean accepted;
    // Valor numerico escrito por el usuario al presionar jugar
    private int selectedValue;
    // Selector del perfil de la máquina
    private JComboBox<String> profileBox;
    // Selector de personaje del jugador 1
    private JComboBox<String> character1Box;
    // Selector de personaje del jugador 2 (o de la máquina)
    private JComboBox<String> character2Box;
    // Opción de partido por tiempo
    private JRadioButton byTimeButton;
    // Opción de partido por goles
    private JRadioButton byGoalsButton;
    // Minutos o goles del partido
    private JSpinner valueSpinner;
    // Etiqueta que dice si el valor son minutos o goles
    private JLabel valueLabel;
    // Botón para iniciar el partido
    private JButton playButton;
    // Botón para volver a la pantalla de inicio
    private JButton cancelButton;

    /**
     * @param owner ventana principal
     * @param mode DOPOSoccer.PLAYER_VS_PLAYER o DOPOSoccer.PLAYER_VS_MACHINE
     */
    public MatchSetupDialog(JFrame owner, String mode) {
        super(owner, DOPOSoccer.PLAYER_VS_MACHINE.equals(mode) ? "Single Player" : "Multiplayer", true);
        this.mode = mode;
        prepareElements();
        prepareActions();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void prepareElements() {
        boolean machine = isMachineMode();
        profileBox = GameStyle.comboBox(DOPOSoccer.MACHINE_PROFILES);
        character1Box = GameStyle.comboBox(CharacterFactory.COUNTRIES);
        character2Box = GameStyle.comboBox(CharacterFactory.COUNTRIES);
        character2Box.setSelectedIndex(1);
        byTimeButton = GameStyle.radioButton(MatchRule.BY_TIME, true);
        byGoalsButton = GameStyle.radioButton(MatchRule.BY_GOALS, false);
        ButtonGroup group = new ButtonGroup();
        group.add(byTimeButton);
        group.add(byGoalsButton);
        valueSpinner = new JSpinner(new SpinnerNumberModel(2, TimeRule.MIN_MINUTES, TimeRule.MAX_MINUTES, 1));
        configureSpinnerEditor();
        GameStyle.styleSpinner(valueSpinner);
        valueLabel = GameStyle.label("Minutos del partido");

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typePanel.setOpaque(false);
        typePanel.add(byTimeButton);
        typePanel.add(Box.createHorizontalStrut(12));
        typePanel.add(byGoalsButton);

        JPanel form = GameStyle.card(new GridBagLayout());
        int row = 0;
        addRow(form, row++, GameStyle.label("Modalidad"), GameStyle.title(mode, 20));
        if (machine) {
            addRow(form, row++, GameStyle.label("Perfil de la máquina"), profileBox);
        }
        addRow(form, row++, GameStyle.label("Personaje jugador 1"), character1Box);
        addRow(form, row++, GameStyle.label(machine ? "Personaje máquina" : "Personaje jugador 2"), character2Box);
        addRow(form, row++, GameStyle.label("Tipo de partido"), typePanel);
        addRow(form, row, valueLabel, valueSpinner);

        playButton = GameStyle.primaryButton("Jugar");
        cancelButton = GameStyle.button("Cancelar");

        GameStyle.decorate(this, getTitle(), form, GameStyle.buttonRow(cancelButton, playButton));
        getRootPane().setDefaultButton(playButton);
    }

    private void prepareActions() {
        byTimeButton.addActionListener(e -> updateValueSpinner());
        byGoalsButton.addActionListener(e -> updateValueSpinner());
        playButton.addActionListener(e -> {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) valueSpinner.getEditor()).getTextField();
            String text = textField.getText().trim();
            try {
                selectedValue = Integer.parseInt(text);
                accepted = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Debe ingresar un número entero válido.",
                        "Valor inválido", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelButton.addActionListener(e -> dispose());
    }

    private void updateValueSpinner() {
        if (byTimeButton.isSelected()) {
            valueLabel.setText("Minutos del partido");
            valueSpinner.setModel(new SpinnerNumberModel(2, TimeRule.MIN_MINUTES, TimeRule.MAX_MINUTES, 1));
        } else {
            valueLabel.setText("Goles para ganar");
            valueSpinner.setModel(new SpinnerNumberModel(5, GoalRule.MIN_GOALS, GoalRule.MAX_GOALS, 1));
        }
        configureSpinnerEditor();
        // Swing crea un campo nuevo al cambiar el modelo: se le vuelve a dar el estilo
        GameStyle.styleSpinnerEditor(valueSpinner);
    }
    
    /**
     * Este evita que el spinner borre y revierta automaicamente el numero invalido
     * cuando el usuario hace clic en jugar, para que pueda ser validado por el dominio
     */
    private void configureSpinnerEditor() {
        JFormattedTextField textField = ((JSpinner.DefaultEditor) valueSpinner.getEditor()).getTextField();
        textField.setFocusLostBehavior(JFormattedTextField.PERSIST);
    }

    private static void addRow(JPanel form, int row, JLabel label, Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(6, 4, 6, 14);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        form.add(label, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(field, c);
    }

    private boolean isMachineMode() {
        return DOPOSoccer.PLAYER_VS_MACHINE.equals(mode);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getMode() {
        return mode;
    }

    public String getProfile() {
        return (String) profileBox.getSelectedItem();
    }

    public String getCharacter1() {
        return (String) character1Box.getSelectedItem();
    }

    public String getCharacter2() {
        return (String) character2Box.getSelectedItem();
    }

    public String getMatchType() {
        return byTimeButton.isSelected() ? MatchRule.BY_TIME : MatchRule.BY_GOALS;
    }

    public int getValue() {
        return selectedValue;
    }
}
