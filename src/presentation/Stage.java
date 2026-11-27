package presentation;

/**
 * Escenarios de la cancha cada uno tiene su imagen
 */
public enum Stage {

    // Pradera 
    MEADOW("Pradera", Sprites.BACKGROUND_MEADOW, 0.914),
    // Cueva 
    CAVE("Cueva", Sprites.BACKGROUND_CAVE, 0.821),
    // Desierto 
    DESERT("Desierto", Sprites.BACKGROUND_DESERT, 0.884);

    // Nombre que se muestra en el selector
    private final String label;
    // Archivo de la imagen de fondo dentro de images/
    private final String image;
    // Altura del suelo dentro de la imagen (0 = arriba, 1 = abajo)
    private final double groundFraction;

    Stage(String label, String image, double groundFraction) {
        this.label = label;
        this.image = image;
        this.groundFraction = groundFraction;
    }

    public String getImage() {
        return image;
    }

    public double getGroundFraction() {
        return groundFraction;
    }

    @Override
    public String toString() {
        return label;
    }
}
