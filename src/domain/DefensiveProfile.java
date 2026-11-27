package domain;

/**
 * Perfil Defensivo: su prioridad es defender el arco.
 */
public class DefensiveProfile extends MachineProfile {

    @Override
    public void decide(DOPOSoccer game, int playerNumber, double dt) {
        Player me = game.getPlayer(playerNumber);
        Ball ball = game.getBall();

        double goalGuardX = me.getOwnGoalDirection() > 0
                ? Field.WIDTH - Field.GOAL_WIDTH - 70
                : Field.GOAL_WIDTH + 70;

        boolean ballInMyHalf = me.getOwnGoalDirection() > 0
                ? ball.getX() >= Field.WIDTH / 2
                : ball.getX() <= Field.WIDTH / 2;

        if (ballInMyHalf) {
            double interceptX = ball.getX() + me.getOwnGoalDirection() * 25;
            moveTowards(game, playerNumber, interceptX, 12);

            if (isBallClose(me, ball, Player.WIDTH / 2 + Player.KICK_REACH, Player.HEIGHT)) {
                if (me.isPowerReady() && !me.hasAura()) {
                    game.activatePower(playerNumber);
                }
                game.kick(playerNumber);
            }

            if (Math.abs(ball.getX() - me.getX()) < 80 && ball.getY() < me.getY() - Player.HEIGHT * 0.7) {
                game.jump(playerNumber);
            }
        } else {
            moveTowards(game, playerNumber, goalGuardX, 15);
        }
    }

    @Override
    public String getName() {
        return DEFENSIVE;
    }
}