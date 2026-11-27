package Test;

import domain.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Suite de pruebas unitarias exhaustiva para DOPOSoccer.
 */
public class DOPOSoccerTest {

    private DOPOSoccer game;

    @Before
    public void setUp() {
        game = new DOPOSoccer();
    }

    @After
    public void tearDown() {
        game = null;
    }

    // 1. PRUEBAS DE VALIDACIÓN Y MODOS DE JUEGO

    @Test
    public void shouldValidateModePlayerVsPlayerSuccessfully() {
        try {
            DOPOSoccer.validateMode(DOPOSoccer.PLAYER_VS_PLAYER);
        } catch (DOPOSoccerException e) {
            fail("No debería lanzar excepción para Jugador vs Jugador: " + e.getMessage());
        }
    }


    @Test
    public void shouldThrowExceptionWhenModeIsArcade() {
        try {
            DOPOSoccer.validateMode(DOPOSoccer.ARCADE);
            fail("Debería haber lanzado DOPOSoccerException por modo no incluido.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.MODE_NOT_INCLUDED, e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenModeIsTournament() {
        try {
            DOPOSoccer.validateMode(DOPOSoccer.TOURNAMENT);
            fail("Debería haber lanzado DOPOSoccerException por modo no incluido.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.MODE_NOT_INCLUDED, e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenModeIsInvalid() {
        try {
            DOPOSoccer.validateMode("ModoLoco");
            fail("Debería haber lanzado DOPOSoccerException por modo inválido.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_MODE, e.getMessage());
        }
    }

    // 2. PRUEBAS DE INICIO Y CICLO DE PARTIDO

    @Test
    public void shouldStartNewMatchSuccessfullyByGoals() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY, MatchRule.BY_GOALS, 5);
            assertTrue(game.isRunning());
            assertFalse(game.isPaused());
            assertFalse(game.isFinished());
            assertNotNull(game.getBall());
            assertEquals("Estados Unidos", game.getPlayer(1).getCharacter().getCountry());
            assertEquals("Camerún", game.getPlayer(2).getCharacter().getCountry());
        } catch (DOPOSoccerException e) {
            fail("El partido nuevo por goles falló: " + e.getMessage());
        }
    }

    @Test
    public void shouldStartNewMatchSuccessfullyByTime() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, Nigeria.COUNTRY, SouthKorea.COUNTRY, MatchRule.BY_TIME, 3);
            assertTrue(game.isRunning());
            assertTrue(game.getRule() instanceof TimeRule);
        } catch (DOPOSoccerException e) {
            fail("El partido nuevo por tiempo falló: " + e.getMessage());
        }
    }

    @Test
    public void shouldTogglePauseCorrectly() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, UnitedStates.COUNTRY, MatchRule.BY_GOALS, 3);
            assertFalse(game.isPaused());
            game.togglePause();
            assertTrue(game.isPaused());
            game.togglePause();
            assertFalse(game.isPaused());
        } catch (DOPOSoccerException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void shouldAbandonMatchCorrectly() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, UnitedStates.COUNTRY, MatchRule.BY_GOALS, 3);
            game.abandon();
            assertTrue(game.isFinished());
            assertNull(game.getWinner());
            assertEquals("Partido abandonado. No se otorgan puntos.", game.getResultMessage());
        } catch (DOPOSoccerException e) {
            fail(e.getMessage());
        }
    }

    // 3. PRUEBAS DE JUGADORES Y ESTADÍSTICAS

    @Test
    public void shouldGetPlayersAndRivalsCorrectly() {
        Player p1 = game.getPlayer(1);
        Player p2 = game.getPlayer(2);
        assertNotNull(p1);
        assertNotNull(p2);
        assertEquals(p2, game.getRival(p1));
        assertEquals(p1, game.getRival(p2));
    }

    @Test
    public void shouldThrowExceptionForInvalidPlayerNumber() {
        try {
            game.getPlayer(3);
            fail("Debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Jugador inexistente"));
        }
    }

    @Test
    public void shouldHandleStatsAndUpgradesProperly() {
        Player p1 = game.getPlayer(1);
        Stats stats = p1.getStats();
        
        try {
            assertEquals(1, stats.get(Stats.SPEED));
            assertEquals(Stats.BASE_UPGRADE_COST, stats.upgradeCost(Stats.SPEED));
            assertFalse(stats.isMax(Stats.SPEED));

            // Intentar mejorar sin puntos debe fallar
            p1.upgradeStat(Stats.SPEED);
            fail("Debería fallar por puntos insuficientes.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.NOT_ENOUGH_POINTS, e.getMessage());
        }

        // Agregar puntos y mejorar
        p1.addPoints(1000);
        try {
            p1.upgradeStat(Stats.SPEED);
            assertEquals(2, stats.get(Stats.SPEED));
        } catch (DOPOSoccerException e) {
            fail("La mejora de estadística falló: " + e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenUpgradingStatDuringMatch() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, UnitedStates.COUNTRY, MatchRule.BY_GOALS, 3);
            game.upgradeStat(1, Stats.SPEED);
            fail("No se puede mejorar estadísticas con el partido iniciado.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.MATCH_IN_PROGRESS, e.getMessage());
        }
    }

    @Test
    public void shouldControlPlayerMovementAndJump() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, UnitedStates.COUNTRY, MatchRule.BY_GOALS, 3);
            game.setDirection(1, 1);
            game.jump(1);
            game.update(0.1);
            assertFalse(game.getPlayer(1).isOnGround());
        } catch (DOPOSoccerException e) {
            fail(e.getMessage());
        }
    }

    // 4. PRUEBAS DE FÁBRICA Y PERSONAJES

    @Test
    public void shouldCreateCharactersSuccessfully() {
        try {
            GameCharacter us = CharacterFactory.create(UnitedStates.COUNTRY);
            assertEquals("Estados Unidos", us.getCountry());
            assertEquals(30, us.getShotDamage());
            assertNotNull(us.createPowerShot());

            GameCharacter cm = CharacterFactory.create(Cameroon.COUNTRY);
            assertEquals("Camerún", cm.getCountry());

            GameCharacter ng = CharacterFactory.create(Nigeria.COUNTRY);
            assertEquals("Nigeria", ng.getCountry());

            GameCharacter kr = CharacterFactory.create(SouthKorea.COUNTRY);
            assertEquals("Corea del Sur", kr.getCountry());
        } catch (DOPOSoccerException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionForInvalidCharacter() {
        try {
            CharacterFactory.create("Atlantis");
            fail("Debería fallar por personaje inexistente.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_CHARACTER, e.getMessage());
        }

        try {
            CharacterFactory.create(null);
            fail("Debería fallar por personaje nulo.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_CHARACTER, e.getMessage());
        }
    }

    // 5. PRUEBAS DE REGLAS DE PARTIDO

    @Test
    public void shouldValidateGoalRuleBounds() {
        try {
            new GoalRule(0);
            fail("Goles menores a min");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_GOALS, e.getMessage());
        }

        try {
            new GoalRule(25);
            fail("Goles mayores a max");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_GOALS, e.getMessage());
        }
    }

    @Test
    public void shouldValidateTimeRuleBounds() {
        try {
            new TimeRule(0);
            fail("Minutos menores a min");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_MINUTES, e.getMessage());
        }

        try {
            new TimeRule(15);
            fail("Minutos mayores a max");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_MINUTES, e.getMessage());
        }
    }

    @Test
    public void shouldDetectWinnerInGoalRule() {
        try {
            GoalRule rule = new GoalRule(3);
            Player p1 = new Player("P1", 1);
            Player p2 = new Player("P2", -1);

            assertNull(rule.checkWinner(p1, p2));
            p1.scoreGoal();
            p1.scoreGoal();
            p1.scoreGoal();
            assertEquals(p1, rule.checkWinner(p1, p2));
        } catch (DOPOSoccerException e) {
            fail(e.getMessage());
        }
    }

    // 6. PRUEBAS DE BALÓN Y FÍSICA

    @Test
    public void shouldUpdateBallPhysicsAndFakeBalls() {
        Ball ball = new Ball(500, 200);
        ball.setVelocity(100, 100);
        ball.update(0.1);
        assertTrue(ball.getX() > 500);

        Ball fake = Ball.createFake(500, 200, 50, 50, 1.0);
        assertTrue(fake.isFake());
        assertFalse(fake.isExpired());
        fake.update(1.1);
        assertTrue(fake.isExpired());
    }

    // 7. PRUEBAS DE TIROS DE PODER Y EFECTOS

    @Test
    public void shouldActivateAndLaunchPowerShots() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY, MatchRule.BY_GOALS, 5);
            Player p1 = game.getPlayer(1);
            
            // Forzar barra de poder llena simulando tiempo o directamente probando efectos
            assertFalse(game.activatePower(1));


            for (int i = 0; i < 110; i++) {
                game.update(0.1);
            }
            
            assertTrue(p1.isPowerReady());
            assertTrue(game.activatePower(1));
            assertTrue(p1.hasAura());

            // Probar efectos individuales
            ConfusionEffect confusion = new ConfusionEffect();
            assertEquals("Confusión", confusion.getName());
            assertTrue(confusion.canMove());

            PushEffect push = new PushEffect();
            assertEquals("Empuje", push.getName());
            assertFalse(push.canMove());

            ElectrocutedEffect electro = new ElectrocutedEffect();
            assertEquals("Electrocutado", electro.getName());
            assertFalse(electro.canJump());
            assertEquals(0.5, electro.getSpeedFactor(), 0.01);

            UnconsciousEffect unconscious = new UnconsciousEffect();
            assertEquals("Inconsciente", unconscious.getName());
            assertFalse(unconscious.canKick());

        } catch (DOPOSoccerException e) {
            fail(e.getMessage());
        }
    }

    // 8. PRUEBAS DE JUGADOR VS MÁQUINA Y PERFILES (STRATEGY + FACTORY)

    @Test
    public void shouldCreateAllValidMachineProfiles() throws DOPOSoccerException {
        assertTrue(MachineProfile.create(MachineProfile.CLUMSY) instanceof ClumsyProfile);
        assertTrue(MachineProfile.create(MachineProfile.OFFENSIVE) instanceof OffensiveProfile);
        assertTrue(MachineProfile.create(MachineProfile.DEFENSIVE) instanceof DefensiveProfile);
        assertTrue(MachineProfile.create(MachineProfile.EXPERT) instanceof ExpertProfile);
    }

    @Test
    public void shouldReturnCorrectNameForEachMachineProfile() throws DOPOSoccerException {
        assertEquals("Torpe", MachineProfile.create(MachineProfile.CLUMSY).getName());
        assertEquals("Ofensiva", MachineProfile.create(MachineProfile.OFFENSIVE).getName());
        assertEquals("Defensiva", MachineProfile.create(MachineProfile.DEFENSIVE).getName());
        assertEquals("Experta", MachineProfile.create(MachineProfile.EXPERT).getName());
    }

    @Test
    public void shouldThrowExceptionWhenMachineProfileIsNull() {
        try {
            MachineProfile.create(null);
            fail("Debería lanzar excepción con perfil nulo.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_MACHINE_PROFILE, e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenMachineProfileIsUnknown() {
        try {
            MachineProfile.create("Imposible");
            fail("Debería lanzar excepción con perfil desconocido.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_MACHINE_PROFILE, e.getMessage());
        }
    }

    @Test
    public void shouldStartPlayerVsMachineMatchSuccessfully() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_MACHINE, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 3, MachineProfile.OFFENSIVE);
        assertTrue(game.isRunning());
        assertTrue(game.isVsMachine());
        assertNotNull(game.getMachineProfile());
        assertEquals(MachineProfile.OFFENSIVE, game.getMachineProfile().getName());
    }

    @Test
    public void shouldNotSetMachineProfileInPlayerVsPlayerMatch() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 3, null);
        assertTrue(game.isRunning());
        assertFalse(game.isVsMachine());
        assertNull(game.getMachineProfile());
    }

    @Test
    public void shouldThrowExceptionWhenStartingVsMachineWithInvalidProfile() {
        try {
            game.newMatch(DOPOSoccer.PLAYER_VS_MACHINE, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                    MatchRule.BY_GOALS, 3, "PerfilFalso");
            fail("Debería lanzar excepción por perfil de máquina inválido.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_MACHINE_PROFILE, e.getMessage());
            assertFalse(game.isRunning());
        }
    }

    @Test
    public void shouldUpdateMachinePlayerDuringMatchUpdate() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_MACHINE, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_TIME, 2, MachineProfile.EXPERT);
        double initialX = game.getPlayer(2).getX();
        game.getBall().setPosition(Field.WIDTH / 2 - 50, Field.GROUND - Ball.RADIUS);
        for (int i = 0; i < 20; i++) {
            game.update(0.05);
        }
        assertNotEquals(initialX, game.getPlayer(2).getX(), 0.001);
    }


    // 9. PRUEBAS DE STATS Y PROGRESIÓN DE PUNTOS

    @Test
    public void shouldInitializeAllStatsAtMinLevelWithBaseFactors() throws DOPOSoccerException {
        Stats stats = new Stats();
        for (String name : Stats.NAMES) {
            assertEquals(Stats.MIN, stats.get(name));
            assertFalse(stats.isMax(name));
            assertEquals(Stats.BASE_UPGRADE_COST, stats.upgradeCost(name));
        }
        assertEquals(1.0, stats.speedFactor(), 0.0001);
        assertEquals(1.0, stats.kickMultiplier(), 0.0001);
        assertEquals(1.0, stats.jumpFactor(), 0.0001);
        assertEquals(Stats.BASE_LIFE, stats.maxLife(), 0.0001);
    }

    @Test
    public void shouldDoubleUpgradeCostAtEachLevel() throws DOPOSoccerException {
        Player p = new Player("P1", 1);
        p.addPoints(10000);
        assertEquals(500, p.getStats().upgradeCost(Stats.SPEED));
        p.upgradeStat(Stats.SPEED);
        assertEquals(1000, p.getStats().upgradeCost(Stats.SPEED));
        p.upgradeStat(Stats.SPEED);
        assertEquals(2000, p.getStats().upgradeCost(Stats.SPEED));
        p.upgradeStat(Stats.SPEED);
        assertEquals(4000, p.getStats().upgradeCost(Stats.SPEED));
    }

    @Test
    public void shouldIncreaseStatFactorsProperly() throws DOPOSoccerException {
        Player p = new Player("P1", 1);
        p.addPoints(10000);
        p.upgradeStat(Stats.SPEED);
        p.upgradeStat(Stats.SPEED);
        assertEquals(1.2, p.getStats().speedFactor(), 0.0001);

        p.upgradeStat(Stats.KICK);
        assertEquals(1.1, p.getStats().kickMultiplier(), 0.0001);

        p.upgradeStat(Stats.JUMP);
        p.upgradeStat(Stats.JUMP);
        p.upgradeStat(Stats.JUMP);
        assertEquals(1.3, p.getStats().jumpFactor(), 0.0001);

        p.upgradeStat(Stats.SURVIVAL);
        assertEquals(110.0, p.getStats().maxLife(), 0.0001);
    }

    @Test
    public void shouldThrowExceptionWhenGettingOrIncreasingInvalidStat() {
        Stats stats = new Stats();
        try {
            stats.get("stamina");
            fail("Debería lanzar excepción por estadística inválida.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_STAT, e.getMessage());
        }

        Player p = new Player("P1", 1);
        p.addPoints(1000);
        try {
            p.upgradeStat("stamina");
            fail("Debería lanzar excepción al aumentar estadística inválida.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.INVALID_STAT, e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenStatReachesMaxLevel() throws DOPOSoccerException {
        Player p1 = game.getPlayer(1);
        for (int i = Stats.MIN; i < Stats.MAX; i++) {
            p1.addPoints(p1.getStats().upgradeCost(Stats.KICK));
            game.upgradeStat(1, Stats.KICK);
        }
        assertEquals(Stats.MAX, p1.getStats().get(Stats.KICK));
        assertTrue(p1.getStats().isMax(Stats.KICK));

        p1.addPoints(100000);
        try {
            game.upgradeStat(1, Stats.KICK);
            fail("Debería lanzar excepción al superar el nivel máximo.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.MAX_STAT, e.getMessage());
        }
    }

    @Test
    public void shouldDeductPointsAndKeepStatsAcrossMatches() throws DOPOSoccerException {
        Player p1 = game.getPlayer(1);
        p1.addPoints(1500);
        game.upgradeStat(1, Stats.SPEED);
        assertEquals(2, p1.getStats().get(Stats.SPEED));
        assertEquals(1000, p1.getPoints());

        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 3);
        assertEquals(2, game.getPlayer(1).getStats().get(Stats.SPEED));
        assertEquals(1000, game.getPlayer(1).getPoints());
    }

    @Test
    public void shouldThrowExceptionWhenUpgradingWithExactMissingPoint() throws DOPOSoccerException {
        Player p1 = game.getPlayer(1);
        p1.addPoints(Stats.BASE_UPGRADE_COST - 1);
        try {
            game.upgradeStat(1, Stats.JUMP);
            fail("Debería fallar si falta 1 punto.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.NOT_ENOUGH_POINTS, e.getMessage());
            assertEquals(Stats.MIN, p1.getStats().get(Stats.JUMP));
            assertEquals(Stats.BASE_UPGRADE_COST - 1, p1.getPoints());
        }
    }

    @Test
    public void shouldThrowExceptionWhenUpgradingMaxedStatViaGame() throws DOPOSoccerException {
        Player p1 = game.getPlayer(1);
        for (int i = Stats.MIN; i < Stats.MAX; i++) {
            p1.addPoints(p1.getStats().upgradeCost(Stats.SURVIVAL));
            game.upgradeStat(1, Stats.SURVIVAL);
        }
        p1.addPoints(100000);
        try {
            game.upgradeStat(1, Stats.SURVIVAL);
            fail("Debería lanzar MAX_STAT.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.MAX_STAT, e.getMessage());
        }
    }


    // 10. PRUEBAS DE JUGADOR, VIDA, ATURDIMIENTO, PATADA Y GOLES

    @Test
    public void shouldPreparePlayerWithMaxLifeAndZeroGoals() throws DOPOSoccerException {
        Player p1 = game.getPlayer(1);
        p1.addPoints(500);
        p1.upgradeStat(Stats.SURVIVAL);
        GameCharacter us = CharacterFactory.create(UnitedStates.COUNTRY);
        p1.prepareForMatch(us, Field.PLAYER1_START);

        assertEquals(us, p1.getCharacter());
        assertEquals(0, p1.getGoals());
        assertEquals(110.0, p1.getLife(), 0.0001);
        assertEquals(110.0, p1.getMaxLife(), 0.0001);
        assertEquals(0.0, p1.getPowerBar(), 0.0001);
        assertFalse(p1.hasAura());
        assertEquals(Field.PLAYER1_START, p1.getX(), 0.0001);
        assertTrue(p1.isOnGround());
    }

    @Test
    public void shouldStunPlayerWhenLifeReachesZeroAndRecoverAfterStunTime() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        Player p1 = game.getPlayer(1);
        p1.receiveDamage(p1.getMaxLife());

        assertEquals(0.0, p1.getLife(), 0.0001);
        assertTrue(p1.isStunned());
        assertFalse(p1.canMove());
        assertFalse(p1.canJump());
        assertFalse(p1.canKick());

        game.update(Player.STUN_TIME + 0.1);
        assertFalse(p1.isStunned());
        assertEquals(p1.getMaxLife(), p1.getLife(), 0.0001);
        assertTrue(p1.canMove());
    }

    @Test
    public void shouldNotTakeMoreDamageWhileAlreadyStunned() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        Player p1 = game.getPlayer(1);
        p1.receiveDamage(p1.getMaxLife());
        double stunBefore = p1.getStunTime();
        p1.receiveDamage(50);
        assertEquals(0.0, p1.getLife(), 0.0001);
        assertEquals(stunBefore, p1.getStunTime(), 0.0001);
    }

    @Test
    public void shouldRespectKickCooldown() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        Player p1 = game.getPlayer(1);
        assertTrue(p1.kick());
        assertTrue(p1.isKicking());
        assertFalse(p1.kick());

        game.update(Player.KICK_COOLDOWN + 0.05);
        assertTrue(p1.kick());
    }

    @Test
    public void shouldNotActivatePowerWhenBarIsNotFull() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.update(2.0);
        assertFalse(game.activatePower(1));
        assertFalse(game.getPlayer(1).hasAura());
    }

    @Test
    public void shouldGiveGoalBonusToConcederPowerBar() {
        Player conceder = new Player("P2", -1);
        assertEquals(0.0, conceder.getPowerBar(), 0.0001);
        conceder.concedeGoal();
        assertEquals(Player.GOAL_BONUS, conceder.getPowerBar(), 0.0001);
    }

   @Test
    public void shouldDamageRivalWhenKickingCloseWithoutBall() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        Player p1 = game.getPlayer(1);
        Player p2 = game.getPlayer(2);
        game.getBall().setPosition(Field.WIDTH / 2, Field.BALL_START_Y);

        game.setDirection(2, -1);
        while (p2.getX() > p1.getX() + Player.WIDTH + 5) {
            game.update(0.05);
        }
        game.setDirection(2, 0);

        double initialLife = p2.getLife();
        game.kick(1);
        assertTrue(p2.getLife() < initialLife);
    }

    @Test
    public void shouldKickBallWhenBallIsInKickZone() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        Player p1 = game.getPlayer(1);
        game.getBall().setPosition(p1.getX() + Player.WIDTH / 2 + 10, Field.GROUND - Ball.RADIUS);
        game.getBall().setVelocity(0, 0);

        game.kick(1);
        assertTrue(game.getBall().getVx() > 0);
    }

    @Test
    public void shouldScoreGoalInRightGoalAndResetPositions() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.getBall().setPosition(Field.WIDTH - 5, Field.GROUND - Ball.RADIUS - 10);
        game.getBall().setVelocity(0, 0);
        game.update(0.02);

        assertEquals(1, game.getPlayer(1).getGoals());
        assertEquals(0, game.getPlayer(2).getGoals());
        assertEquals(Field.PLAYER1_START, game.getPlayer(1).getX(), 0.001);
        assertEquals(Field.WIDTH / 2.0, game.getBall().getX(), 0.001);
    }

    @Test
    public void shouldScoreGoalInLeftGoalForPlayer2() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.getBall().setPosition(5, Field.GROUND - Ball.RADIUS - 10);
        game.getBall().setVelocity(0, 0);
        game.update(0.02);

        assertEquals(1, game.getPlayer(2).getGoals());
        assertEquals(0, game.getPlayer(1).getGoals());
    }

    @Test
    public void shouldAwardWinnerAndLoserPointsWhenMatchEndsByGoals() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 1);
        game.getBall().setPosition(Field.WIDTH - 5, Field.GROUND - Ball.RADIUS - 10);
        game.getBall().setVelocity(0, 0);
        game.update(0.02);

        assertTrue(game.isFinished());
        assertFalse(game.isRunning());
        assertEquals(game.getPlayer(1), game.getWinner());
        assertEquals(DOPOSoccer.WINNER_POINTS, game.getPlayer(1).getPoints());
        assertEquals(DOPOSoccer.LOSER_POINTS, game.getPlayer(2).getPoints());
    }

    @Test
    public void shouldFinishMatchAndDeclareLeaderWhenFinishIsCalled() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.getPlayer(2).scoreGoal();
        game.finish();

        assertTrue(game.isFinished());
        assertEquals(game.getPlayer(2), game.getWinner());
        assertEquals(DOPOSoccer.WINNER_POINTS, game.getPlayer(2).getPoints());
        assertEquals(DOPOSoccer.LOSER_POINTS, game.getPlayer(1).getPoints());
    }

    @Test
    public void shouldNotUpdateMatchWhenPaused() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.setDirection(1, 1);
        game.togglePause();
        double xBefore = game.getPlayer(1).getX();
        game.update(0.5);
        assertEquals(xBefore, game.getPlayer(1).getX(), 0.0001);
    }

    @Test
    public void shouldNotJumpOrKickWhilePaused() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.togglePause();
        game.jump(1);
        game.kick(1);
        assertTrue(game.getPlayer(1).isOnGround());
        assertFalse(game.getPlayer(1).isKicking());
    }

    @Test
    public void shouldThrowExceptionWhenGettingPlayerBelowOne() {
        try {
            game.getPlayer(0);
            fail("Debería lanzar IllegalArgumentException para jugador 0.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Jugador inexistente"));
        }
    }


    // 11. PRUEBAS DE TIMERULE, BALL Y EFECTOS SOBRE PLAYER

    @Test
    public void shouldDetectSuddenDeathWhenTimeExpiresWithTie() throws DOPOSoccerException {
        TimeRule rule = new TimeRule(1);
        Player p1 = new Player("P1", 1);
        Player p2 = new Player("P2", -1);
        rule.update(60.0);

        assertTrue(rule.isTimeOver());
        assertTrue(rule.isSuddenDeath(p1, p2));
        assertNull(rule.checkWinner(p1, p2));
        assertEquals("MUERTE SÚBITA", rule.getClockText());
    }

    @Test
    public void shouldDetectWinnerWhenTimeExpiresWithoutTie() throws DOPOSoccerException {
        TimeRule rule = new TimeRule(1);
        Player p1 = new Player("P1", 1);
        Player p2 = new Player("P2", -1);
        p1.scoreGoal();
        rule.update(60.0);

        assertTrue(rule.isTimeOver());
        assertFalse(rule.isSuddenDeath(p1, p2));
        assertEquals(p1, rule.checkWinner(p1, p2));
    }

    @Test
    public void shouldNotExpireTimeRuleBeforeDuration() throws DOPOSoccerException {
        TimeRule rule = new TimeRule(2);
        Player p1 = new Player("P1", 1);
        Player p2 = new Player("P2", -1);
        rule.update(59.0);

        assertFalse(rule.isTimeOver());
        assertNull(rule.checkWinner(p1, p2));
    }

    @Test
    public void shouldBounceBallOnGroundAndBorders() {
        Ball ball = new Ball(Field.WIDTH / 2.0, Field.GROUND - Ball.RADIUS - 2);
        ball.setVelocity(0, 300);
        ball.update(0.1);
        assertTrue(ball.getY() <= Field.GROUND - Ball.RADIUS);
        assertTrue(ball.getVy() < 0);
    }

    @Test
    public void shouldClearShotAndRestoreBallPhysics() {
        Ball ball = new Ball(100, 100);
        ball.setGravityScale(0.0);
        ball.setRestitution(1.0);
        ball.clearShot();
        ball.setVelocity(0, 200);
        ball.update(0.1);
        assertTrue(ball.getVy() > 200);
    }

    @Test
    public void shouldIncludeFakeBallsInGetBalls() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        assertEquals(1, game.getBalls().size());
        assertFalse(game.getBalls().get(0).isFake());

        Ball fake = Ball.createFake(300, 200, 50, 0, 2.0);
        assertTrue(fake.isFake());
        assertFalse(fake.isExpired());
    }

    @Test
    public void shouldRemoveExpiredFakeBallsOnUpdate() {
        Ball fake = Ball.createFake(300, 200, 50, 0, 0.5);
        assertFalse(fake.isExpired());
        fake.update(0.6);
        assertTrue(fake.isExpired());
    }

    @Test
    public void shouldApplyAndExpireEffectOnPlayer() {
        Player p = new Player("P1", 1);
        ElectrocutedEffect electro = new ElectrocutedEffect();
        p.applyEffect(electro);
        assertEquals(electro, p.getEffect());
        assertEquals(0.5, p.getCurrentSpeed() / Player.BASE_SPEED, 0.001);

        for (int i = 0; i < 100; i++) {
            p.update(0.1);
        }
        assertNull(p.getEffect());
    }

    @Test
    public void shouldReplaceExistingEffectWithNewEffect() {
        Player p = new Player("P1", 1);
        p.applyEffect(new ElectrocutedEffect());
        PushEffect push = new PushEffect();
        p.applyEffect(push);
        assertEquals(push, p.getEffect());
        assertFalse(p.canMove());
    }

    @Test
    public void shouldIncreaseJumpHeightWhenJumpStatIsUpgraded() throws DOPOSoccerException {
        Player basePlayer = new Player("Base", 1);
        Player upgradedPlayer = new Player("Upgraded", 1);
        upgradedPlayer.addPoints(500);
        upgradedPlayer.upgradeStat(Stats.JUMP);

        basePlayer.prepareForMatch(CharacterFactory.create(UnitedStates.COUNTRY), 200);
        upgradedPlayer.prepareForMatch(CharacterFactory.create(UnitedStates.COUNTRY), 200);

        basePlayer.jump();
        upgradedPlayer.jump();

        assertTrue(Math.abs(upgradedPlayer.getVy()) > Math.abs(basePlayer.getVy()));
    }

    @Test
    public void shouldIncreaseSpeedWhenSpeedStatIsUpgraded() throws DOPOSoccerException {
        Player basePlayer = new Player("Base", 1);
        Player upgradedPlayer = new Player("Upgraded", 1);
        upgradedPlayer.addPoints(500);
        upgradedPlayer.upgradeStat(Stats.SPEED);

        assertTrue(upgradedPlayer.getCurrentSpeed() > basePlayer.getCurrentSpeed());
    }

    @Test
    public void shouldNotLeaveFieldBoundsWhenMoving() throws DOPOSoccerException {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.setDirection(1, -1);
        for (int i = 0; i < 100; i++) {
            game.update(0.1);
        }
        assertTrue(game.getPlayer(1).getX() >= Player.WIDTH / 2.0);
        assertTrue(game.getPlayer(1).getX() <= Field.WIDTH - Player.WIDTH / 2.0);
    }

    // 12. PRUEBAS DE PERSISTENCIA (SALVAR Y ABRIR)

    // archivo temporal que se borra al terminar
    private static File tempFile() throws IOException {
        File file = File.createTempFile("doposoccer", "." + DOPOSoccer.FILE_EXTENSION);
        file.deleteOnExit();
        return file;
    }

    @Test
    public void shouldSaveAndOpenMatchInProgress() throws Exception {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, UnitedStates.COUNTRY, Cameroon.COUNTRY,
                MatchRule.BY_GOALS, 5);
        game.getPlayer(1).scoreGoal();
        game.getPlayer(2).scoreGoal();
        game.getPlayer(2).scoreGoal();
        for (int i = 0; i < 10; i++) {
            game.update(0.1);
        }
        double ballX = game.getBall().getX();
        double elapsed = game.getRule().getElapsed();
        File file = tempFile();

        game.save(file);
        DOPOSoccer loaded = DOPOSoccer.open(file);

        assertTrue(loaded.isRunning());
        assertEquals(1, loaded.getPlayer(1).getGoals());
        assertEquals(2, loaded.getPlayer(2).getGoals());
        assertEquals(UnitedStates.COUNTRY, loaded.getPlayer(1).getCharacter().getCountry());
        assertEquals(Cameroon.COUNTRY, loaded.getPlayer(2).getCharacter().getCountry());
        assertEquals(ballX, loaded.getBall().getX(), 0.0001);
        assertEquals(elapsed, loaded.getRule().getElapsed(), 0.0001);
    }

    @Test
    public void shouldKeepPointsAndStatsWhenSavingAndOpening() throws Exception {
        game.getPlayer(1).addPoints(1500);
        game.getPlayer(1).upgradeStat(Stats.KICK);
        File file = tempFile();

        game.save(file);
        DOPOSoccer loaded = DOPOSoccer.open(file);

        assertFalse(loaded.isRunning());
        assertEquals(1000, loaded.getPlayer(1).getPoints());
        assertEquals(2, loaded.getPlayer(1).getStats().get(Stats.KICK));
    }

    @Test
    public void shouldKeepMachineProfileAndPauseWhenSavingAndOpening() throws Exception {
        game.newMatch(DOPOSoccer.PLAYER_VS_MACHINE, Nigeria.COUNTRY, SouthKorea.COUNTRY,
                MatchRule.BY_TIME, 3, MachineProfile.EXPERT);
        game.togglePause();
        File file = tempFile();

        game.save(file);
        DOPOSoccer loaded = DOPOSoccer.open(file);

        assertTrue(loaded.isVsMachine());
        assertEquals(MachineProfile.EXPERT, loaded.getMachineProfile().getName());
        assertTrue(loaded.isPaused());
        assertTrue(loaded.getRule() instanceof TimeRule);
    }

    @Test
    public void shouldContinuePlayingAfterOpening() throws Exception {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, SouthKorea.COUNTRY, Nigeria.COUNTRY,
                MatchRule.BY_GOALS, 3);
        File file = tempFile();
        game.save(file);
        DOPOSoccer loaded = DOPOSoccer.open(file);

        double before = loaded.getRule().getElapsed();
        loaded.update(0.1);

        assertTrue(loaded.getRule().getElapsed() > before);
    }

    @Test
    public void shouldNotChangeSavedMatchWhenOriginalKeepsPlaying() throws Exception {
        game.newMatch(DOPOSoccer.PLAYER_VS_PLAYER, SouthKorea.COUNTRY, Nigeria.COUNTRY,
                MatchRule.BY_GOALS, 3);
        File file = tempFile();
        game.save(file);
        game.getPlayer(1).scoreGoal();

        DOPOSoccer loaded = DOPOSoccer.open(file);

        assertEquals(0, loaded.getPlayer(1).getGoals());
    }

    @Test
    public void shouldThrowExceptionWhenSavingWithoutFile() {
        try {
            game.save(null);
            fail("Debería fallar si no hay archivo.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.NO_FILE, e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenOpeningMissingFile() {
        try {
            DOPOSoccer.open(new File("no_existe_" + System.nanoTime() + ".dopo"));
            fail("Debería fallar si el archivo no existe.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.FILE_NOT_FOUND, e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenOpeningInvalidFile() throws IOException {
        File file = tempFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("esto no es una partida");
        }
        try {
            DOPOSoccer.open(file);
            fail("Debería fallar si el archivo no es una partida.");
        } catch (DOPOSoccerException e) {
            assertEquals(DOPOSoccerException.OPEN_ERROR, e.getMessage());
        }
    }
}
