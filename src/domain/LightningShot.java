package domain;

/**
 * Rayo (Camerún): recto, cubierto de electricidad.
 */
public class LightningShot extends PowerShot {

    // Velocidad horizontal del tiro
    public static final double SPEED = 900;

    public LightningShot(int baseDamage) {
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
        return new ElectrocutedEffect();
    }

    @Override
    public String getName() {
        return "Rayo";
    }
}
