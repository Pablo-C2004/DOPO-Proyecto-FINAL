package domain;

/**
 * Partido por tiempo: gana quien tenga más goles al final.
 * Si hay empate, el partido entra en muerte súbita.
 */
public class TimeRule extends MatchRule {

    // Mínimo de minutos que se puede configurar
    public static final int MIN_MINUTES = 1;
    // Máximo de minutos que se puede configurar
    public static final int MAX_MINUTES = 10;

    // Duración del partido en minutos
    private final int minutes;

    public TimeRule(int minutes) throws DOPOSoccerException {
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MINUTES);
        }
        this.minutes = minutes;
    }

    public boolean isTimeOver() {
        return elapsed >= minutes * 60.0;
    }

    public boolean isSuddenDeath(Player p1, Player p2) {
        return isTimeOver() && p1.getGoals() == p2.getGoals();
    }

    @Override
    public Player checkWinner(Player p1, Player p2) {
        if (!isTimeOver()) {
            return null;
        }
        return leader(p1, p2);
    }

    @Override
    public String getClockText() {
        if (isTimeOver()) {
            return "MUERTE SÚBITA";
        }
        return format(minutes * 60.0 - elapsed);
    }

    @Override
    public String getDescription() {
        return BY_TIME + " (" + minutes + " min)";
    }

    public int getMinutes() {
        return minutes;
    }
}
