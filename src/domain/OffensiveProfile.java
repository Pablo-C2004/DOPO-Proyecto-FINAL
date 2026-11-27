package domain;

/**
 * Perfil Ofensivo: su prioridad es anotar.
 */
public class OffensiveProfile extends MachineProfile {

    @Override
    public void decide(DOPOSoccer game, int playerNumber, double dt) {
        Player me = game.getPlayer(playerNumber);
        Ball ball = game.getBall();

        if (me.isPowerReady() && !me.hasAura()) {
            game.activatePower(playerNumber);
        }

        double targetX = ball.getX() + me.getOwnGoalDirection() * (Player.WIDTH / 2);
        moveTowards(game, playerNumber, targetX, 15);

        if (isBallClose(me, ball, Player.WIDTH / 2 + Player.KICK_REACH, Player.HEIGHT)) {
            game.kick(playerNumber);
        }

        if (Math.abs(ball.getX() - me.getX()) < 90 && ball.getY() < me.getY() - Player.HEIGHT * 0.8) {
            game.jump(playerNumber);
        }
    }

    @Override
    public String getName() {
        return OFFENSIVE;
    }
}