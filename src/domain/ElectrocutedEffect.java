package domain;

/**
 * Electrocutado (Camerún): no puede saltar y se mueve al 50 % de su velocidad durante 3 s.
 */
public class ElectrocutedEffect extends Effect {

    // Duración del efecto en segundos
    public static final double DURATION = 3;
    // Factor de velocidad mientras está electrocutado
    public static final double SPEED_FACTOR = 0.5;

    public ElectrocutedEffect() {
        super(DURATION);
    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public double getSpeedFactor() {
        return SPEED_FACTOR;
    }

    @Override
    public String getName() {
        return "Electrocutado";
    }
}
