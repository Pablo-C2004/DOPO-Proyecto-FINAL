package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Efecto que un tiro de poder deja sobre el rival durante un tiempo.
 * Para crear un efecto nuevo extender esta clase y sobreescribir
 * las restricciones que aplique.
 */
public abstract class Effect implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    // Duración total del efecto en segundos
    private final double duration;
    // Tiempo que le queda al efecto
    private double remaining;

    protected Effect(double duration) {
        this.duration = duration;
        this.remaining = duration;
    }

    /**
     * Se ejecuta una sola vez cuando el efecto empieza.
     */
    public void start(Player victim) {
    }

    /**
     * Avanza el efecto dt segundos.
     */
    public final void update(Player victim, double dt) {
        act(victim, dt);
        remaining -= dt;
    }

    /**
     * Acción que el efecto hace en cada instante (por defecto ninguna).
     */
    protected void act(Player victim, double dt) {
    }

    public boolean isFinished() {
        return remaining <= 0;
    }

    public boolean canMove() {
        return true;
    }

    public boolean canJump() {
        return true;
    }

    public boolean canKick() {
        return true;
    }

    /**
     * Factor que multiplica la velocidad del jugador afectado.
     */
    public double getSpeedFactor() {
        return 1;
    }

    public double getDuration() {
        return duration;
    }

    public double getRemaining() {
        return Math.max(0, remaining);
    }

    public abstract String getName();
}
