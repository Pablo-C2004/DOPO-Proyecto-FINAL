package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Condición para ganar el partido. Hay dos tipos: por tiempo y por goles.
 */
public abstract class MatchRule implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    // Nombre del tipo de partido por tiempo
    public static final String BY_TIME = "Por tiempo";
    // Nombre del tipo de partido por goles
    public static final String BY_GOALS = "Por goles";
    // Tipos de partido disponibles
    public static final String[] TYPES = {BY_TIME, BY_GOALS};

    // Segundos transcurridos del partido
    protected double elapsed;

    /**
     * Crea la regla del partido.
     * @param type BY_TIME o BY_GOALS
     * @param value minutos del partido o goles para ganar
     */
    public static MatchRule create(String type, int value) throws DOPOSoccerException {
        if (BY_TIME.equals(type)) {
            return new TimeRule(value);
        }
        if (BY_GOALS.equals(type)) {
            return new GoalRule(value);
        }
        throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_TYPE);
    }

    public void update(double dt) {
        elapsed += dt;
    }

    /**
     * @return el ganador o null si todavía no hay ganador
     */
    public abstract Player checkWinner(Player p1, Player p2);

    /**
     * Texto del reloj que se muestra en pantalla.
     */
    public abstract String getClockText();

    public abstract String getDescription();

    public double getElapsed() {
        return elapsed;
    }

    protected static String format(double seconds) {
        int total = (int) Math.ceil(Math.max(0, seconds));
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    protected static Player leader(Player p1, Player p2) {
        if (p1.getGoals() > p2.getGoals()) {
            return p1;
        }
        if (p2.getGoals() > p1.getGoals()) {
            return p2;
        }
        return null;
    }
}
