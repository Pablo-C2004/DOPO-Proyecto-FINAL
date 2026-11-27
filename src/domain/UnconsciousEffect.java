package domain;

/**
 * Inconsciente (Nigeria): es lanzado al aire y no puede moverse, saltar ni patear durante 3 s.
 */
public class UnconsciousEffect extends Effect {

    // Duración del efecto en segundos
    public static final double DURATION = 3;
    // Velocidad con la que el rival sale lanzado al aire
    public static final double LAUNCH_SPEED = 800;

    public UnconsciousEffect() {
        super(DURATION);
    }

    @Override
    public void start(Player victim) {
        victim.launchUp(LAUNCH_SPEED);
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public boolean canKick() {
        return false;
    }

    @Override
    public String getName() {
        return "Inconsciente";
    }
}
