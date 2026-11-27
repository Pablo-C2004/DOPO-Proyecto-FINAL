package domain;

/**
 * Empuje (Corea del Sur): el rival es lanzado hacia su propio arco durante 1,5 s.
 */
public class PushEffect extends Effect {

    // Duración del efecto en segundos
    public static final double DURATION = 1.5;
    // Velocidad con la que el rival es empujado
    public static final double PUSH_SPEED = 650;

    public PushEffect() {
        super(DURATION);
    }

    @Override
    protected void act(Player victim, double dt) {
        victim.forceHorizontalVelocity(victim.getOwnGoalDirection() * PUSH_SPEED);
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public String getName() {
        return "Empuje";
    }
}
