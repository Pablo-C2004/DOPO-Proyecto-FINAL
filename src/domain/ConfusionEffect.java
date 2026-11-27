package domain;

/**
 * Confusión (Estados Unidos): el rival no sabe cuál de los 8 balones es el real
 * durante 2,5 s. No restringe el movimiento.
 */
public class ConfusionEffect extends Effect {

    // Duración del efecto en segundos
    public static final double DURATION = 2.5;

    public ConfusionEffect() {
        super(DURATION);
    }

    @Override
    public String getName() {
        return "Confusión";
    }
}
