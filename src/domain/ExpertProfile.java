package domain;

/**
 * Perfil Experto: decide la acción según el estado del partido
 * combina estrategias ofensivas y defensivas.
 */
public class ExpertProfile extends MachineProfile {

    private final OffensiveProfile offensive = new OffensiveProfile();
    private final DefensiveProfile defensive = new DefensiveProfile();

    @Override
    public void decide(DOPOSoccer game, int playerNumber, double dt) {
        Player me = game.getPlayer(playerNumber);
        Player rival = game.getRival(me);
        Ball ball = game.getBall();

        boolean winning = me.getGoals() > rival.getGoals();
        boolean ballNearMyGoal = me.getOwnGoalDirection() > 0
                ? ball.getX() > Field.WIDTH * 0.60
                : ball.getX() < Field.WIDTH * 0.40;

        if (winning || ballNearMyGoal) {
            defensive.decide(game, playerNumber, dt);
        } else {
            offensive.decide(game, playerNumber, dt);
        }
    }

    @Override
    public String getName() {
        return EXPERT;
    }
}