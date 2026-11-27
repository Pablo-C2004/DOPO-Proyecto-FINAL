package domain;

import java.util.Random;

/**
 * Perfil Torpe: escoge al azar sus movimientos.
 */
public class ClumsyProfile extends MachineProfile {

    private static final double DECISION_INTERVAL = 0.3;
    private final Random random = new Random();
    private double timer = 0;

    @Override
    public void decide(DOPOSoccer game, int playerNumber, double dt) {
        timer -= dt;
        if (timer > 0) {
            return;
        }
        timer = DECISION_INTERVAL;

        // Dirección aleatoria: -1, 0 o 1
        int dir = random.nextInt(3) - 1;
        game.setDirection(playerNumber, dir);

        if (random.nextDouble() < 0.25) {
            game.jump(playerNumber);
        }
        if (random.nextDouble() < 0.30) {
            game.kick(playerNumber);
        }
        if (random.nextDouble() < 0.20) {
            game.activatePower(playerNumber);
        }
    }

    @Override
    public String getName() {
        return CLUMSY;
    }
}