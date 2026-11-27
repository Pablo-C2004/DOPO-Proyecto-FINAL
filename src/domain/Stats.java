package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Estadísticas de un jugador: speed, kick, jump y survival.
 * Todas empiezan en 1 y pueden subir hasta 10. Cada punto por encima de 1
 * representa un 10 % más sobre el valor base.
 */
public class Stats implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    // Valor mínimo de una estadística
    public static final int MIN = 1;
    // Valor máximo de una estadística
    public static final int MAX = 10;
    // Precio del primer punto de mejora
    public static final int BASE_UPGRADE_COST = 500;
    // Vida base con Survival 1
    public static final double BASE_LIFE = 100;
    // Incremento por cada punto de estadística (10 %)
    public static final double INCREMENT = 0.10;

    // Nombre de la estadística velocidad
    public static final String SPEED = "speed";
    // Nombre de la estadística patada
    public static final String KICK = "kick";
    // Nombre de la estadística salto
    public static final String JUMP = "jump";
    // Nombre de la estadística vida
    public static final String SURVIVAL = "survival";
    // Nombres de todas las estadísticas
    public static final String[] NAMES = {SPEED, KICK, JUMP, SURVIVAL};

    // Nivel de velocidad
    private int speed;
    // Nivel de patada
    private int kick;
    // Nivel de salto
    private int jump;
    // Nivel de vida
    private int survival;

    public Stats() {
        speed = MIN;
        kick = MIN;
        jump = MIN;
        survival = MIN;
    }

    /**
     * Factor genérico: 1 + 0,10 * (valor - 1).
     */
    private static double factor(int value) {
        return 1 + INCREMENT * (value - 1);
    }

    /**
     * multiplicadorKick = 1 + 0,10 * (Kick - 1)
     */
    public double kickMultiplier() {
        return factor(kick);
    }

    /**
     * vidaMax = 100 * (1 + 0,10 * (Survival - 1))
     */
    public double maxLife() {
        return Math.round(BASE_LIFE * factor(survival));
    }

    public double speedFactor() {
        return factor(speed);
    }

    public double jumpFactor() {
        return factor(jump);
    }

    /**
     * Valor actual de una estadística.
     * @param stat speed, kick, jump o survival
     */
    public int get(String stat) throws DOPOSoccerException {
        switch (stat) {
            case SPEED: return speed;
            case KICK: return kick;
            case JUMP: return jump;
            case SURVIVAL: return survival;
            default: throw new DOPOSoccerException(DOPOSoccerException.INVALID_STAT);
        }
    }

    /**
     * Precio del siguiente punto de mejora: 500, 1000, 2000, ...
     * (se duplica con respecto al anterior).
     */
    public int upgradeCost(String stat) throws DOPOSoccerException {
        int level = get(stat);
        return BASE_UPGRADE_COST * (1 << (level - 1));
    }

    /**
     * Indica si la estadística ya llegó a su máximo.
     */
    public boolean isMax(String stat) throws DOPOSoccerException {
        return get(stat) >= MAX;
    }

    /**
     * Sube un punto la estadística. No cobra el cobro lo hace el jugador.
     */
    void increase(String stat) throws DOPOSoccerException {
        if (isMax(stat)) {
            throw new DOPOSoccerException(DOPOSoccerException.MAX_STAT);
        }
        switch (stat) {
            case SPEED: speed++; break;
            case KICK: kick++; break;
            case JUMP: jump++; break;
            case SURVIVAL: survival++; break;
            default: throw new DOPOSoccerException(DOPOSoccerException.INVALID_STAT);
        }
    }

    public int getSpeed() {
        return speed;
    }

    public int getKick() {
        return kick;
    }

    public int getJump() {
        return jump;
    }

    public int getSurvival() {
        return survival;
    }
}
