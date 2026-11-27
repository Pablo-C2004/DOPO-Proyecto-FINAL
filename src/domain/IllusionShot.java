package domain;

// Generador de números aleatorios
import java.util.Random;

/**
 * Ilusión (Estados Unidos) el balón se divide en 8 balones idénticos;
 * 7 son falsos y desaparecen al terminar el efecto (2,5 s).
 */
public class IllusionShot extends PowerShot {

    // Cantidad total de balones (1 real + 7 falsos)
    public static final int BALLS = 8;
    // Velocidad horizontal de cada balón
    public static final double SPEED = 650;
    // Velocidad vertical de cada uno de los 8 balones
    public static final double[] VERTICAL_SPEEDS = {-850, -700, -550, -400, -250, -100, 50, 200};

    // Generador aleatorio para escoger cuál es el balón real
    private static final Random RANDOM = new Random();

    public IllusionShot(int baseDamage) {
        super(baseDamage);
    }

    @Override
    protected void configure(DOPOSoccer game, Ball ball, int direction) {
        int real = RANDOM.nextInt(BALLS);
        for (int i = 0; i < BALLS; i++) {
            double vx = direction * SPEED;
            double vy = VERTICAL_SPEEDS[i];
            if (i == real) {
                ball.setVelocity(vx, vy);
            } else {
                game.addFakeBall(Ball.createFake(ball.getX(), ball.getY(), vx, vy, getDuration()));
            }
        }
    }

    @Override
    protected double getDuration() {
        return ConfusionEffect.DURATION;
    }

    @Override
    public Effect createEffect() {
        return new ConfusionEffect();
    }

    @Override
    public String getName() {
        return "Ilusión";
    }
}
