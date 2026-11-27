package domain;

/**
 * Excepción propia de DOPOSoccer.
 */
public class DOPOSoccerException extends Exception {

    // Modalidad desconocida
    public static final String INVALID_MODE = "La modalidad de juego seleccionada no es válida.";
    // Modalidad que llega en la versión dos
    public static final String MODE_NOT_AVAILABLE = "La modalidad Jugador vs Máquina estará disponible en la versión dos.";
    // Modos del juego original que DOPOSoccer no incluye
    public static final String MODE_NOT_INCLUDED = "Los modos Arcade y Torneo no hacen parte de esta versión de DOPOSoccer.";
    // Personaje inexistente
    public static final String INVALID_CHARACTER = "El personaje seleccionado no existe.";
    // Tipo de partido inválido
    public static final String INVALID_MATCH_TYPE = "El tipo de partido debe ser 'Por tiempo' o 'Por goles'.";
    // Minutos fuera de rango
    public static final String INVALID_MINUTES = "Los minutos del partido deben estar entre 1 y 10.";
    // Goles fuera de rango
    public static final String INVALID_GOALS = "Los goles para ganar deben estar entre 1 y 20.";
    // Estadística inexistente
    public static final String INVALID_STAT = "La estadística indicada no existe (speed, kick, jump o survival).";
    // Estadística ya en su máximo
    public static final String MAX_STAT = "La estadística ya está en su valor máximo (10).";
    // Puntos insuficientes para mejorar
    public static final String NOT_ENOUGH_POINTS = "No tiene puntos suficientes para esta mejora.";
    // Intento de mejorar durante un partido
    public static final String MATCH_IN_PROGRESS = "Las estadísticas solo se pueden mejorar antes de iniciar un partido.";
    // No se escogió archivo
    public static final String NO_FILE = "Debe indicar un archivo para guardar o abrir la partida.";
    // Error al guardar
    public static final String SAVE_ERROR = "No se pudo guardar la partida en el archivo indicado.";
    // El archivo no existe
    public static final String FILE_NOT_FOUND = "El archivo de la partida no existe.";
    // Error al abrir
    public static final String OPEN_ERROR = "No se pudo abrir la partida: el archivo está dañado o no es una partida de DOPOSoccer.";
    // Perfil de maquina inexistente
    public static final String INVALID_MACHINE_PROFILE = "El perfil de la maquina seleccionado no es valido";

    public DOPOSoccerException(String message) {
        super(message);
    }
}
