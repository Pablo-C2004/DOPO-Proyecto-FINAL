package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Balón del partido. Está afectado por la gravedad y rebota en el suelo,
 * en los arcos y en los bordes de la cancha.
 * Puede llevar un tiro de poder activo. Los balones falsos (Ilusión)
 * tienen un tiempo de vida y luego desaparecen.
 */
public class Ball implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    // Radio del balón
    public static final double RADIUS = 15;
    // Fracción de velocidad que conserva al rebotar
    public static final double RESTITUTION = 0.75;
    // Fricción horizontal al tocar el suelo
    public static final double GROUND_FRICTION = 0.99;
    // Velocidad mínima de rebote; por debajo el balón se queda quieto
    public static final double MIN_BOUNCE = 60;

    // Posición horizontal del centro
    private double x;
    // Posición vertical del centro
    private double y;
    // Velocidad horizontal
    private double vx;
    // Velocidad vertical (positiva hacia abajo)
    private double vy;
    // Posición vertical en el cuadro anterior (para detectar el travesaño)
    private double prevY;
    // Multiplicador de la gravedad (0 = tiro recto)
    private double gravityScale;
    // Rebote actual (cambia con algunos tiros de poder)
    private double restitution;
    // Tiro de poder activo en el balón (null si es un balón normal)
    private PowerShot shot;
    // Indica si es un balón falso de Ilusión
    private final boolean fake;
    // Tiempo de vida restante de un balón falso
    private double lifeTime;

    public Ball(double x, double y) {
        this(x, y, false);
    }

    private Ball(double x, double y, boolean fake) {
        this.x = x;
        this.y = y;
        this.prevY = y;
        this.fake = fake;
        this.gravityScale = 1;
        this.restitution = RESTITUTION;
    }

    /**
     * Crea un balón falso con velocidad y tiempo de vida.
     */
    public static Ball createFake(double x, double y, double vx, double vy, double lifeTime) {
        Ball ball = new Ball(x, y, true);
        ball.setVelocity(vx, vy);
        ball.lifeTime = lifeTime;
        return ball;
    }

    /**
     * Avanza la física del balón dt segundos.
     */
    public void update(double dt) {
        prevY = y;
        if (fake) {
            lifeTime -= dt;
        }
        vy += Field.GRAVITY * gravityScale * dt;
        x += vx * dt;
        y += vy * dt;
        bounceOnBorders();
        bounceOnCrossbars();
    }

    private void bounceOnBorders() {
        if (y + RADIUS > Field.GROUND) {
            y = Field.GROUND - RADIUS;
            vy = -Math.abs(vy) * restitution;
            vx *= GROUND_FRICTION;
            if (Math.abs(vy) < MIN_BOUNCE && gravityScale > 0) {
                vy = 0;
            }
        }
        if (y - RADIUS < 0) {
            y = RADIUS;
            vy = Math.abs(vy) * restitution;
        }
        if (x - RADIUS < 0) {
            x = RADIUS;
            vx = Math.abs(vx) * restitution;
        }
        if (x + RADIUS > Field.WIDTH) {
            x = Field.WIDTH - RADIUS;
            vx = -Math.abs(vx) * restitution;
        }
    }

    private void bounceOnCrossbars() {
        boolean overLeftBar = x < Field.GOAL_WIDTH;
        boolean overRightBar = x > Field.WIDTH - Field.GOAL_WIDTH;
        if ((overLeftBar || overRightBar)
                && prevY + RADIUS <= Field.CROSSBAR_Y && y + RADIUS > Field.CROSSBAR_Y) {
            y = Field.CROSSBAR_Y - RADIUS;
            vy = -Math.abs(vy) * restitution;
        }
        bounceOnPoint(Field.GOAL_WIDTH, Field.CROSSBAR_Y);
        bounceOnPoint(Field.WIDTH - Field.GOAL_WIDTH, Field.CROSSBAR_Y);
    }

    /**
     * Rebote contra la punta de un travesaño.
     */
    private void bounceOnPoint(double px, double py) {
        double dx = x - px;
        double dy = y - py;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist >= RADIUS || dist == 0) {
            return;
        }
        double nx = dx / dist;
        double ny = dy / dist;
        x = px + nx * RADIUS;
        y = py + ny * RADIUS;
        double vn = vx * nx + vy * ny;
        if (vn < 0) {
            vx -= (1 + restitution) * vn * nx;
            vy -= (1 + restitution) * vn * ny;
        }
    }

    /**
     * Quita el tiro de poder y devuelve la física normal.
     */
    public void clearShot() {
        shot = null;
        gravityScale = 1;
        restitution = RESTITUTION;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        this.prevY = y;
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public void setGravityScale(double gravityScale) {
        this.gravityScale = gravityScale;
    }

    public void setRestitution(double restitution) {
        this.restitution = restitution;
    }

    void setShot(PowerShot shot) {
        this.shot = shot;
    }

    public PowerShot getShot() {
        return shot;
    }

    public boolean hasShot() {
        return shot != null;
    }

    public boolean isFake() {
        return fake;
    }

    public boolean isExpired() {
        return fake && lifeTime <= 0;
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
}
