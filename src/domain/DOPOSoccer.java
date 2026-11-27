package domain;

// Archivo donde se guarda o se abre una partida
import java.io.File;
// leer el archivo
import java.io.FileInputStream;
// escribir en el archivo
import java.io.FileOutputStream;
// error al leer o escribir el archivo
import java.io.IOException;
// leer la partida desde el archivo
import java.io.ObjectInputStream;
// escribir la partida en el archivo
import java.io.ObjectOutputStream;
// para poder guardar el objeto en un archivo
import java.io.Serializable;
// Lista dinámica basada en arreglo
import java.util.ArrayList;
// Utilidades de colecciones (lista no modificable)
import java.util.Collections;
// Recorrido de una colección permitiendo eliminar elementos
import java.util.Iterator;
// Interfaz de lista
import java.util.List;

/**
 * Fachada Maneja un partido de DOPOSoccer entre dos
 * jugadores: física, toques del balón, patadas, poderes, goles y ganador.
 * Los jugadores (con sus puntos y estadísticas) se conservan entre partidos.
 * La partida se puede guardar en un archivo y abrir después.
 */
public class DOPOSoccer implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;
    // Extensión de los archivos de partida
    public static final String FILE_EXTENSION = "dopo";

    // Nombre de la modalidad jugador contra jugador
    public static final String PLAYER_VS_PLAYER = "Jugador vs Jugador";
    // Nombre de la modalidad jugador contra máquina
    public static final String PLAYER_VS_MACHINE = "Jugador vs Máquina";
    // Modalidades de juego disponibles
    public static final String[] MODES = {PLAYER_VS_PLAYER, PLAYER_VS_MACHINE};
    // Modo Arcade del juego original (no incluido en DOPOSoccer)
    public static final String ARCADE = "Arcade";
    // Modo Torneo del juego original (no incluido en DOPOSoccer)
    public static final String TOURNAMENT = "Torneo";
    // Perfiles posibles de la máquina
    public static final String[] MACHINE_PROFILES = {"Torpe", "Ofensiva", "Defensiva", "Experta"};

    // Puntos que recibe el ganador
    public static final int WINNER_POINTS = 100;
    // Puntos que recibe el perdedor
    public static final int LOSER_POINTS = 25;
    // Fuerza horizontal con la que una patada impulsa el balón
    public static final double KICK_FORCE_X = 750;
    // Fuerza vertical con la que una patada levanta el balón
    public static final double KICK_FORCE_Y = 500;
    // Rebote del balón al chocar con el cuerpo de un jugador
    public static final double TOUCH_RESTITUTION = 0.6;

    // Jugador 1 (defiende el arco izquierdo)
    private final Player player1;
    // Jugador 2 (defiende el arco derecho)
    private final Player player2;
    // Balón real del partido
    private Ball ball;
    // Balones falsos creados por el tiro Ilusión
    private final List<Ball> fakeBalls;
    // Regla del partido (por tiempo o por goles)
    private MatchRule rule;
    // Perfil de la maquina si se juega jugador vs maquina (null en jugador vs jugador)
    private MachineProfile machineProfile;
    // Indica si ya se inició algún partido
    private boolean started;
    // Indica si el partido está en pausa
    private boolean paused;
    // Indica si el partido ya terminó
    private boolean finished;
    // Ganador del partido (null si no hay)
    private Player winner;
    // Mensaje final con el ganador y los puntos
    private String resultMessage;

    public DOPOSoccer() {
        player1 = new Player("Jugador 1", 1);
        player2 = new Player("Jugador 2", -1);
        fakeBalls = new ArrayList<>();
        ball = new Ball(Field.WIDTH / 2, Field.BALL_START_Y);
    }

    /**
     * Verifica que la modalidad se pueda jugar.
     * @param mode modalidad escogida en la pantalla de inicio
     */
    
    public static void validateMode(String mode) throws DOPOSoccerException {
        if (ARCADE.equals(mode) || TOURNAMENT.equals(mode)) {
            throw new DOPOSoccerException(DOPOSoccerException.MODE_NOT_INCLUDED);
        }
        if (!PLAYER_VS_PLAYER.equals(mode) && !PLAYER_VS_MACHINE.equals(mode)) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MODE);
        }
    }

    /**
     * Inicia un partido nuevo con la configuración indicada.
     * @param mode modalidad (por ahora solo Jugador vs Jugador)
     * @param country1 personaje del jugador 1
     * @param country2 personaje del jugador 2
     * @param matchType MatchRule.BY_TIME o MatchRule.BY_GOALS
     * @param value minutos o goles para ganar
     */
    public void newMatch(String mode, String country1, String country2, String matchType, int value)
            throws DOPOSoccerException {
        newMatch(mode, country1, country2, matchType, value, null);
    }

    public void newMatch(String mode, String country1, String country2, String matchType, int value, String profileName)
            throws DOPOSoccerException {
        validateMode(mode);
        GameCharacter c1 = CharacterFactory.create(country1);
        GameCharacter c2 = CharacterFactory.create(country2);
        MatchRule newRule = MatchRule.create(matchType, value);
        MachineProfile newMachine = PLAYER_VS_MACHINE.equals(mode)
                ? MachineProfile.create(profileName)
                : null;

        rule = newRule;
        machineProfile = newMachine;
        player1.prepareForMatch(c1, Field.PLAYER1_START);
        player2.prepareForMatch(c2, Field.PLAYER2_START);
        ball = new Ball(Field.WIDTH / 2, Field.BALL_START_Y);
        fakeBalls.clear();
        started = true;
        paused = false;
        finished = false;
        winner = null;
        resultMessage = "";
    }

    /**
     * Avanza el partido dt segundos.
     */
    public void update(double dt) {
        if (!isRunning() || paused) {
            return;
        }
        rule.update(dt);
        if (machineProfile != null) {
            machineProfile.decide(this, 2, dt);
        }
        player1.update(dt);
        player2.update(dt);
        separatePlayers();

        ball.update(dt);
        updateShot(dt);
        handleTouch(player1);
        handleTouch(player2);
        updateFakeBalls(dt);

        checkGoal();
        checkWinner();
    }
    // Acciones de los jugadores 
    public void setDirection(int playerNumber, int direction) {
        getPlayer(playerNumber).setDirection(direction);
    }

    public void jump(int playerNumber) {
        if (isRunning() && !paused) {
            getPlayer(playerNumber).jump();
        }
    }

    /**
     * Patada. Si alcanza el balón, solo lo impulsa (o ejecuta el tiro de poder
     * si el jugador tiene aura). Si no alcanza el balón pero sí al rival, le hace daño.
     */
    public void kick(int playerNumber) {
        if (!isRunning() || paused) {
            return;
        }
        Player p = getPlayer(playerNumber);
        Player rival = getRival(p);
        if (!p.kick()) {
            return;
        }
        double[] zone = p.getKickZone();
        if (circleTouchesRect(ball.getX(), ball.getY(), Ball.RADIUS, zone)) {
            if (ball.hasShot() && ball.getShot().getShooter() == p) {
                return;
            }
            if (ball.hasShot()) {
                endShot();
            }
            if (p.hasAura()) {
                launchPowerShot(p);
            } else {
                ball.setVelocity(p.getAttackDirection() * KICK_FORCE_X, -KICK_FORCE_Y);
            }
        } else if (rectsTouch(zone, bodyOf(rival))) {
            rival.receiveDamage(Player.KICK_DAMAGE * p.getStats().kickMultiplier());
        }
    }

    /**
     * @return true si el poder se activó
     */
    public boolean activatePower(int playerNumber) {
        if (!isRunning() || paused) {
            return false;
        }
        return getPlayer(playerNumber).activatePower();
    }
    //Control del partido 
    public void togglePause() {
        if (isRunning()) {
            paused = !paused;
        }
    }

    /**
     * Termina el juego gana quien vaya por delante.
     */
    public void finish() {
        if (isRunning()) {
            endMatch(MatchRule.leader(player1, player2));
        }
    }

    /**
     * Abandona el partido sin entregar puntos.
     */
    public void abandon() {
        if (isRunning()) {
            finished = true;
            winner = null;
            resultMessage = "Partido abandonado. No se otorgan puntos.";
        }
    }

    /**
     * Mejora una estadística de un jugador (solo fuera de un partido).
     */
    public void upgradeStat(int playerNumber, String stat) throws DOPOSoccerException {
        if (isRunning()) {
            throw new DOPOSoccerException(DOPOSoccerException.MATCH_IN_PROGRESS);
        }
        getPlayer(playerNumber).upgradeStat(stat);
    }

    /**
     * Guarda la partida en un archivo, con los jugadores y el partido si hay uno.
     */
    public void save(File file) throws DOPOSoccerException {
        if (file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NO_FILE);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        } catch (IOException e) {
            Log.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.SAVE_ERROR);
        }
    }

    /**
     * Abre una partida guardada.
     */
    public static DOPOSoccer open(File file) throws DOPOSoccerException {
        if (file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NO_FILE);
        }
        if (!file.isFile()) {
            throw new DOPOSoccerException(DOPOSoccerException.FILE_NOT_FOUND);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object read = in.readObject();
            if (!(read instanceof DOPOSoccer)) {
                throw new DOPOSoccerException(DOPOSoccerException.OPEN_ERROR);
            }
            return (DOPOSoccer) read;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            Log.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.OPEN_ERROR);
        }
    }
    //Lógica interna 
    private void updateShot(double dt) {
        if (ball.hasShot()) {
            ball.getShot().update(ball, dt);
            if (ball.getShot().isFinished()) {
                endShot();
            }
        }
    }

    /**
     * Qué pasa cuando el balón toca el cuerpo de un jugador.
     */
    private void handleTouch(Player p) {
        if (!circleTouchesRect(ball.getX(), ball.getY(), Ball.RADIUS, bodyOf(p))) {
            return;
        }
        PowerShot shot = ball.getShot();
        if (shot != null) {
            if (shot.getShooter() == p) {
                return;
            }
            p.receiveDamage(shot.getDamage());
            p.applyEffect(shot.createEffect());
            endShot();
            bounce(p);
            return;
        }
        if (p.hasAura()) {
            launchPowerShot(p);
            return;
        }
        bounce(p);
    }

    private void launchPowerShot(Player p) {
        PowerShot shot = p.getCharacter().createPowerShot();
        p.consumeAura();
        double bx = p.getX() + p.getAttackDirection() * (Player.WIDTH / 2 + Ball.RADIUS + 2);
        double by = Math.min(ball.getY(), Field.GROUND - Ball.RADIUS);
        ball.setPosition(bx, by);
        shot.launch(this, ball, p);
    }

    private void endShot() {
        if (ball.hasShot()) {
            ball.getShot().finish();
            ball.clearShot();
        }
    }

    /**
     * Rebote del balón contra el cuerpo de un jugador.
     */
    private void bounce(Player p) {
        double cx = clamp(ball.getX(), p.left(), p.right());
        double cy = clamp(ball.getY(), p.top(), p.bottom());
        double dx = ball.getX() - cx;
        double dy = ball.getY() - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double nx;
        double ny;
        if (dist == 0) {
            nx = 0;
            ny = -1;
        } else {
            nx = dx / dist;
            ny = dy / dist;
        }
        ball.setPosition(cx + nx * (Ball.RADIUS + 0.5), cy + ny * (Ball.RADIUS + 0.5));
        double rvx = ball.getVx() - p.getVx();
        double rvy = ball.getVy() - p.getVy();
        double vn = rvx * nx + rvy * ny;
        if (vn < 0) {
            double newVx = ball.getVx() - (1 + TOUCH_RESTITUTION) * vn * nx;
            double newVy = ball.getVy() - (1 + TOUCH_RESTITUTION) * vn * ny;
            ball.setVelocity(newVx, newVy);
        }
    }

    private void separatePlayers() {
        if (!rectsTouch(bodyOf(player1), bodyOf(player2))) {
            return;
        }
        double overlap = Math.min(player1.right(), player2.right()) - Math.max(player1.left(), player2.left());
        double shift = overlap / 2 + 0.5;
        Player leftOne = player1.getX() <= player2.getX() ? player1 : player2;
        Player rightOne = leftOne == player1 ? player2 : player1;
        moveX(leftOne, -shift);
        moveX(rightOne, shift);
    }

    private void moveX(Player p, double dx) {
        double newX = clamp(p.getX() + dx, Player.WIDTH / 2, Field.WIDTH - Player.WIDTH / 2);
        p.forceX(newX);
    }

    private void updateFakeBalls(double dt) {
        Iterator<Ball> it = fakeBalls.iterator();
        while (it.hasNext()) {
            Ball fake = it.next();
            fake.update(dt);
            if (fake.isExpired()) {
                it.remove();
            }
        }
    }

    private void checkGoal() {
        int side = Field.goalAt(ball.getX(), ball.getY());
        if (side == Field.NO_GOAL) {
            return;
        }
        Player scorer = side == Field.RIGHT_GOAL ? player1 : player2;
        Player conceder = getRival(scorer);
        scorer.scoreGoal();
        conceder.concedeGoal();
        resetAfterGoal();
    }

    /**
     * Después de cada gol, los personajes vuelven a su lugar y el balón cae desde el centro.
     */
    private void resetAfterGoal() {
        endShot();
        fakeBalls.clear();
        player1.resetPosition(Field.PLAYER1_START);
        player2.resetPosition(Field.PLAYER2_START);
        ball = new Ball(Field.WIDTH / 2, Field.BALL_START_Y);
    }

    private void checkWinner() {
        if (finished) {
            return;
        }
        Player w = rule.checkWinner(player1, player2);
        if (w != null) {
            endMatch(w);
        }
    }

    private void endMatch(Player matchWinner) {
        finished = true;
        winner = matchWinner;
        String score = player1.getGoals() + " - " + player2.getGoals();
        if (matchWinner == null) {
            resultMessage = "Partido terminado en empate (" + score + "). No se otorgan puntos.";
            return;
        }
        Player loser = getRival(matchWinner);
        matchWinner.addPoints(WINNER_POINTS);
        loser.addPoints(LOSER_POINTS);
        resultMessage = "¡Ganó " + matchWinner.getName() + " (" + matchWinner.getCharacter() + ")! "
                + "Marcador " + score + ".\n"
                + matchWinner.getName() + " obtiene " + WINNER_POINTS + " puntos (total " + matchWinner.getPoints() + ").\n"
                + loser.getName() + " obtiene " + LOSER_POINTS + " puntos (total " + loser.getPoints() + ").";
    }

    /**
     * Usado por el tiro Ilusión para crear los balones falsos.
     */
    void addFakeBall(Ball fake) {
        fakeBalls.add(fake);
    }
    //Utilidades geométricas
    private static double[] bodyOf(Player p) {
        return new double[] {p.left(), p.top(), p.right(), p.bottom()};
    }

    private static boolean circleTouchesRect(double x, double y, double r, double[] rect) {
        double cx = clamp(x, rect[0], rect[2]);
        double cy = clamp(y, rect[1], rect[3]);
        double dx = x - cx;
        double dy = y - cy;
        return dx * dx + dy * dy < r * r;
    }

    private static boolean rectsTouch(double[] a, double[] b) {
        return a[0] < b[2] && b[0] < a[2] && a[1] < b[3] && b[1] < a[3];
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
    //Consultas
    public Player getPlayer(int number) {
        if (number == 1) {
            return player1;
        }
        if (number == 2) {
            return player2;
        }
        throw new IllegalArgumentException("Jugador inexistente: " + number);
    }

    public Player getRival(Player p) {
        return p == player1 ? player2 : player1;
    }

    public Ball getBall() {
        return ball;
    }

    /**
     * Todos los balones visibles (el real y los falsos).
     */
    public List<Ball> getBalls() {
        List<Ball> all = new ArrayList<>(fakeBalls);
        all.add(ball);
        return Collections.unmodifiableList(all);
    }

    public MatchRule getRule() {
        return rule;
    }

    public boolean isRunning() {
        return started && !finished;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isFinished() {
        return finished;
    }

    public Player getWinner() {
        return winner;
    }

    public String getResultMessage() {
        return resultMessage;
    }
    
    public boolean isVsMachine() {
        return machineProfile != null;
    }
    
    public MachineProfile getMachineProfile() {
        return machineProfile;
    }
}
