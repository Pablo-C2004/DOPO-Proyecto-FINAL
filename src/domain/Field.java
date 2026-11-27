package domain;

/**
 * Dimensiones lógicas de la cancha. El dominio siempre trabaja con estas
 * medidas; la presentación las escala según la resolución elegida.
 */
public final class Field {

    // Ancho lógico de la cancha
    public static final double WIDTH = 1000;
    // Alto lógico de la cancha
    public static final double HEIGHT = 500;
    // Altura del suelo (los pies de los jugadores)
    public static final double GROUND = 450;
    // Ancho (profundidad) de cada arco
    public static final double GOAL_WIDTH = 60;
    // Altura del travesaño
    public static final double CROSSBAR_Y = 300;
    // Aceleración de la gravedad
    public static final double GRAVITY = 1500;

    // Posición inicial del jugador 1
    public static final double PLAYER1_START = 250;
    // Posición inicial del jugador 2
    public static final double PLAYER2_START = 750;
    // Altura desde la que cae el balón en el centro
    public static final double BALL_START_Y = 80;

    // Código del arco izquierdo
    public static final int LEFT_GOAL = -1;
    // Código del arco derecho
    public static final int RIGHT_GOAL = 1;
    // Código de que no hay gol
    public static final int NO_GOAL = 0;

    private Field() {
    }

    /**
     * Indica si el punto (x, y) está dentro de algún arco.
     * @return LEFT_GOAL, RIGHT_GOAL o NO_GOAL
     */
    public static int goalAt(double x, double y) {
        if (y <= CROSSBAR_Y) {
            return NO_GOAL;
        }
        if (x < GOAL_WIDTH) {
            return LEFT_GOAL;
        }
        if (x > WIDTH - GOAL_WIDTH) {
            return RIGHT_GOAL;
        }
        return NO_GOAL;
    }
}
