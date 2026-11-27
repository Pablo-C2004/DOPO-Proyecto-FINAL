package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Estrategia base para controlar a un jugador manejado por la máquina (Patrón Strategy).
 * También actúa como fábrica para crear los perfiles disponibles.
 */
public abstract class MachineProfile implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    public static final String CLUMSY = "Torpe";
    public static final String OFFENSIVE = "Ofensiva";
    public static final String DEFENSIVE = "Defensiva";
    public static final String EXPERT = "Experta";

    /**
     * Crea el perfil de máquina según su nombre.
     */
    public static MachineProfile create(String profile) throws DOPOSoccerException {
        if (profile == null) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MACHINE_PROFILE);
        }
        switch (profile) {
            case CLUMSY: return new ClumsyProfile();
            case OFFENSIVE: return new OffensiveProfile();
            case DEFENSIVE: return new DefensiveProfile();
            case EXPERT: return new ExpertProfile();
            default: throw new DOPOSoccerException(DOPOSoccerException.INVALID_MACHINE_PROFILE);
        }
    }

    /**
     * Decide y ejecuta las acciones de la máquina en el instante actual.
     */
    public abstract void decide(DOPOSoccer game, int playerNumber, double dt);

    public abstract String getName();

    protected void moveTowards(DOPOSoccer game, int playerNumber, double targetX, double tolerance) {
        Player me = game.getPlayer(playerNumber);
        double diff = targetX - me.getX();
        if (Math.abs(diff) <= tolerance) {
            game.setDirection(playerNumber, 0);
        } else {
            game.setDirection(playerNumber, diff > 0 ? 1 : -1);
        }
    }

    protected boolean isBallClose(Player me, Ball ball, double maxDistX, double maxDistY) {
        return Math.abs(ball.getX() - me.getX()) <= maxDistX
                && Math.abs(ball.getY() - (me.getY() - Player.HEIGHT / 2)) <= maxDistY;
    }
}