package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Jugador de DOPOSoccer. Guarda su progreso (estadísticas y puntos), que se
 * mantiene entre partidos, y su estado dentro del partido actual
 * (personaje, goles, vida, barra de poder, aura, efecto y posición).
 */
public class Player implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    // Ancho del cuerpo del jugador
    public static final double WIDTH = 64;
    // Alto del cuerpo del jugador
    public static final double HEIGHT = 96;
    // Velocidad base de desplazamiento
    public static final double BASE_SPEED = 320;
    // Altura base del salto
    public static final double BASE_JUMP_HEIGHT = 120;
    // Segundos que tarda en llenarse la barra de poder
    public static final double POWER_FILL_TIME = 10;
    // Fracción de barra que se gana al recibir un gol
    public static final double GOAL_BONUS = 0.25;
    // Segundos que dura el aturdimiento
    public static final double STUN_TIME = 3;
    // Daño base de una patada
    public static final double KICK_DAMAGE = 10;
    // Duración de la animación de patada
    public static final double KICK_TIME = 0.25;
    // Tiempo mínimo entre dos patadas
    public static final double KICK_COOLDOWN = 0.35;
    // Alcance del pie hacia adelante
    public static final double KICK_REACH = 45;
    // Altura de la zona del pie
    public static final double FOOT_HEIGHT = 40;

    // Nombre visible (Jugador 1 / Jugador 2)
    private final String name;
    // Dirección de ataque: +1 derecha, -1 izquierda
    private final int attackDirection;
    // Estadísticas del jugador (se conservan entre partidos)
    private final Stats stats;
    // Puntos acumulados para comprar mejoras
    private int points;

    // Personaje (país) elegido
    private GameCharacter character;
    // Goles anotados en el partido
    private int goals;
    // Vida actual
    private double life;
    // Progreso de la barra de poder (0 a 1)
    private double powerBar;
    // Indica si tiene el poder activo (aura)
    private boolean aura;
    // Efecto que tiene encima (null si ninguno)
    private Effect effect;
    // Tiempo de aturdimiento restante
    private double stunTime;

    // Posición horizontal (centro del cuerpo)
    private double x;
    // Posición vertical (pies)
    private double y;
    // Velocidad horizontal
    private double vx;
    // Velocidad vertical
    private double vy;
    // Dirección que pide el teclado: -1, 0 o 1
    private int direction;
    // Indica si está tocando el suelo
    private boolean onGround;
    // Tiempo restante de la patada en curso
    private double kickTime;
    // Tiempo que falta para poder patear otra vez
    private double kickCooldown;

    /**
     * @param name nombre visible del jugador
     * @param attackDirection +1 si ataca hacia la derecha, -1 si ataca hacia la izquierda
     */
    public Player(String name, int attackDirection) {
        this.name = name;
        this.attackDirection = attackDirection;
        this.stats = new Stats();
        this.points = 0;
    }

    /**
     * Deja al jugador listo para un partido nuevo 0 goles, vida máxima, barra vacía.
     */
    public void prepareForMatch(GameCharacter character, double startX) {
        this.character = character;
        goals = 0;
        life = stats.maxLife();
        powerBar = 0;
        aura = false;
        effect = null;
        stunTime = 0;
        resetPosition(startX);
    }

    /**
     * Ubica al jugador en el suelo en la posición indicada.
     */
    public void resetPosition(double startX) {
        x = startX;
        y = Field.GROUND;
        vx = 0;
        vy = 0;
        direction = 0;
        onGround = true;
        kickTime = 0;
        kickCooldown = 0;
    }

    /**
     * Avanza el estado del jugador dt segundos.
     */
    public void update(double dt) {
        powerBar = Math.min(1, powerBar + dt / POWER_FILL_TIME);
        kickTime = Math.max(0, kickTime - dt);
        kickCooldown = Math.max(0, kickCooldown - dt);
        if (stunTime > 0) {
            stunTime -= dt;
            if (stunTime <= 0) {
                stunTime = 0;
                life = stats.maxLife();
            }
        }
        vx = canMove() ? direction * getCurrentSpeed() : 0;
        if (effect != null) {
            effect.update(this, dt);
            if (effect.isFinished()) {
                effect = null;
            }
        }
        vy += Field.GRAVITY * dt;
        x += vx * dt;
        y += vy * dt;
        if (y >= Field.GROUND) {
            y = Field.GROUND;
            vy = 0;
            onGround = true;
        } else {
            onGround = false;
        }
        x = Math.max(WIDTH / 2, Math.min(Field.WIDTH - WIDTH / 2, x));
    }

    /**
     * -1 izquierda, 0 quieto, 1 derecha.
     */
    public void setDirection(int direction) {
        this.direction = Integer.signum(direction);
    }

    public void jump() {
        if (canJump() && onGround) {
            double height = BASE_JUMP_HEIGHT * stats.jumpFactor();
            vy = -Math.sqrt(2 * Field.GRAVITY * height);
            onGround = false;
        }
    }

    /**
     * Inicia una patada.
     * @return true si la patada se realizó
     */
    public boolean kick() {
        if (!canKick() || kickCooldown > 0) {
            return false;
        }
        kickTime = KICK_TIME;
        kickCooldown = KICK_COOLDOWN;
        return true;
    }

    /**
     * Activa el poder si la barra está llena y no tiene ya un aura.
     * La barra se vacía y empieza a recargarse en ese mismo instante.
     * @return true si el poder se activó
     */
    public boolean activatePower() {
        if (isStunned() || aura || powerBar < 1) {
            return false;
        }
        aura = true;
        powerBar = 0;
        return true;
    }

    /**
     * Resta vida; la vida nunca es menor que cero. Al llegar a cero queda aturdido.
     */
    public void receiveDamage(double damage) {
        if (isStunned()) {
            return;
        }
        life = Math.max(0, life - damage);
        if (life == 0) {
            stunTime = STUN_TIME;
        }
    }

    /**
     * Aplica un efecto; los efectos no se acumulan, el nuevo reemplaza al anterior.
     */
    public void applyEffect(Effect newEffect) {
        effect = newEffect;
        effect.start(this);
    }

    /**
     * Recibir un gol llena de inmediato un cuarto de la barra de poder.
     */
    public void concedeGoal() {
        powerBar = Math.min(1, powerBar + GOAL_BONUS);
    }

    public void scoreGoal() {
        goals++;
    }

    /**
     * El aura desaparece al ejecutar el tiro de poder.
     */
    public void consumeAura() {
        aura = false;
    }

    public void addPoints(int amount) {
        points += amount;
    }

    /**
     * Compra un punto de mejora para una estadística.
     */
    public void upgradeStat(String stat) throws DOPOSoccerException {
        if (stats.isMax(stat)) {
            throw new DOPOSoccerException(DOPOSoccerException.MAX_STAT);
        }
        int cost = stats.upgradeCost(stat);
        if (points < cost) {
            throw new DOPOSoccerException(DOPOSoccerException.NOT_ENOUGH_POINTS);
        }
        stats.increase(stat);
        points -= cost;
    }
    //Usado por los efectos
    void forceHorizontalVelocity(double newVx) {
        vx = newVx;
    }

    void forceX(double newX) {
        x = newX;
    }

    void launchUp(double speed) {
        vy = -speed;
        onGround = false;
    }
    //Consultas de estado 
    public boolean canMove() {
        return !isStunned() && (effect == null || effect.canMove());
    }

    public boolean canJump() {
        return !isStunned() && (effect == null || effect.canJump());
    }

    public boolean canKick() {
        return !isStunned() && (effect == null || effect.canKick());
    }

    public double getCurrentSpeed() {
        double factor = effect == null ? 1 : effect.getSpeedFactor();
        return BASE_SPEED * stats.speedFactor() * factor;
    }

    public boolean isStunned() {
        return stunTime > 0;
    }

    public boolean isKicking() {
        return kickTime > 0;
    }

    public boolean isPowerReady() {
        return powerBar >= 1;
    }
    //Geometría rectángulo del cuerpo y zona del pie
    public double left() {
        return x - WIDTH / 2;
    }

    public double right() {
        return x + WIDTH / 2;
    }

    public double top() {
        return y - HEIGHT;
    }

    public double bottom() {
        return y;
    }

    /**
     * Zona de alcance del pie: {izquierda, arriba, derecha, abajo}. 
     */
    public double[] getKickZone() {
        double near = x;
        double far = x + attackDirection * (WIDTH / 2 + KICK_REACH);
        return new double[] {Math.min(near, far), y - FOOT_HEIGHT, Math.max(near, far), y};
    }

    /**
     * Dirección hacia el propio arco.
     */
    public int getOwnGoalDirection() {
        return -attackDirection;
    }
    //Getters
    public String getName() {
        return name;
    }

    public int getAttackDirection() {
        return attackDirection;
    }

    public Stats getStats() {
        return stats;
    }

    public int getPoints() {
        return points;
    }

    public GameCharacter getCharacter() {
        return character;
    }

    public int getGoals() {
        return goals;
    }

    public double getLife() {
        return life;
    }

    public double getMaxLife() {
        return stats.maxLife();
    }

    public double getPowerBar() {
        return powerBar;
    }

    public boolean hasAura() {
        return aura;
    }

    public Effect getEffect() {
        return effect;
    }

    public double getStunTime() {
        return stunTime;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public boolean isOnGround() {
        return onGround;
    }
}
