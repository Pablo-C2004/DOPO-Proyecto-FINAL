package domain;

/**
 * Aura Azul (Corea del Sur) tiro recto y horizontal, a alta velocidad.
 */
public class BlueAuraShot extends PowerShot {

    // Velocidad horizontal del tiro
    public static final double SPEED = 1100;

    public BlueAuraShot(int baseDamage) {
        super(baseDamage);
    }

    @Override
    protected void configure(DOPOSoccer game, Ball ball, int direction) {
        ball.setVelocity(direction * SPEED, 0);
        ball.setGravityScale(0);
        ball.setRestitution(1);
    }

    @Override
    public Effect createEffect() {
        return new PushEffect();
    }

    @Override
    public String getName() {
        return "Aura Azul";
    }
}
