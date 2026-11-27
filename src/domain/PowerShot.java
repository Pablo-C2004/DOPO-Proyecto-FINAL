package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Tiro de poder. Cada personaje crea el suyo. Define cómo sale el balón,
 * el daño base (DañoTiro) y el efecto que deja al golpear al rival.
 */
public abstract class PowerShot implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    // Duración máxima de un tiro de poder
    public static final double DEFAULT_DURATION = 2.5;

    // Daño base del tiro (DañoTiro)
    private final int baseDamage;
    // Jugador que ejecutó el tiro
    protected Player shooter;
    // Segundos desde que salió el tiro
    private double elapsed;
    // Indica si el tiro ya terminó
    private boolean finished;

    protected PowerShot(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    /**
     * Ejecuta el tiro y le da su trayectoria.
     */
    public final void launch(DOPOSoccer game, Ball ball, Player shooter) {
        this.shooter = shooter;
        this.elapsed = 0;
        this.finished = false;
        ball.setShot(this);
        configure(game, ball, shooter.getAttackDirection());
    }

    /**
     * Define la trayectoria inicial del balón.
     */
    protected abstract void configure(DOPOSoccer game, Ball ball, int direction);

    /**
     * Crea el efecto que recibe el rival si el tiro lo golpea.
     */
    public abstract Effect createEffect();

    public abstract String getName();

    /**
     * Avanza el tiro al terminar su duración el balón vuelve a ser normal.
     */
    public void update(Ball ball, double dt) {
        elapsed += dt;
        if (elapsed >= getDuration()) {
            finished = true;
        }
    }

    protected double getDuration() {
        return DEFAULT_DURATION;
    }

    /**
     * DañoPoder = DañoTiro × multiplicadorKick del que patea.
     */
    public double getDamage() {
        return baseDamage * shooter.getStats().kickMultiplier();
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public Player getShooter() {
        return shooter;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        finished = true;
    }
}
