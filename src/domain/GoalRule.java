package domain;

/**
 * Partido por goles: gana el primero que llegue al número de goles, sin límite de tiempo.
 */
public class GoalRule extends MatchRule {

    // Mínimo de goles para ganar que se puede configurar
    public static final int MIN_GOALS = 1;
    // Máximo de goles para ganar que se puede configurar
    public static final int MAX_GOALS = 20;

    // Goles necesarios para ganar
    private final int goalsToWin;

    public GoalRule(int goalsToWin) throws DOPOSoccerException {
        if (goalsToWin < MIN_GOALS || goalsToWin > MAX_GOALS) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_GOALS);
        }
        this.goalsToWin = goalsToWin;
    }

    @Override
    public Player checkWinner(Player p1, Player p2) {
        if (p1.getGoals() >= goalsToWin) {
            return p1;
        }
        if (p2.getGoals() >= goalsToWin) {
            return p2;
        }
        return null;
    }

    @Override
    public String getClockText() {
        return format(elapsed);
    }

    @Override
    public String getDescription() {
        return BY_GOALS + " (a " + goalsToWin + ")";
    }

    public int getGoalsToWin() {
        return goalsToWin;
    }
}
