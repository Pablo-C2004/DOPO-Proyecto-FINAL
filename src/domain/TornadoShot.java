package domain;

/**
 * Tornado (Nigeria) remolino a ras de suelo que rebota rápidamente.
 */
public class TornadoShot extends PowerShot {

    // Velocidad horizontal del tornado
    public static final double SPEED = 700;
    // Velocidad vertical inicial de los rebotes
    public static final double BOUNCE_SPEED = 250;
    // Gravedad aumentada para que rebote rápido y bajo
    public static final double GRAVITY_SCALE = 2.5;

    public TornadoShot(int baseDamage) {
        super(baseDamage);
    }

    @Override
    protected void configure(DOPOSoccer game, Ball ball, int direction) {
        ball.setPosition(ball.getX(), Field.GROUND - Ball.RADIUS - 5);
        ball.setVelocity(direction * SPEED, -BOUNCE_SPEED);
        ball.setGravityScale(GRAVITY_SCALE);
        ball.setRestitution(1);
    }

    @Override
    public Effect createEffect() {
        return new UnconsciousEffect();
    }

    @Override
    public String getName() {
        return "Tornado";
    }
}
