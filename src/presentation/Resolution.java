package presentation;

/**
 * Tamaños de la ventana del juego. El dominio usa una pantalla lógica de
 * 1000 x 500; la presentación la estira o encoge (sin deformarla) para
 * llenar la ventana, así que cualquier tamaño funciona.
 * PANTALLA_COMPLETA ocupa todo el monitor sin bordes.
 */
public enum Resolution {

    // Resolución baja
    LOW("Baja (800 x 400)", 800, 400, false),
    // Resolución media (la pantalla lógica tal cual)
    MEDIUM("Media (1000 x 500)", 1000, 500, false),
    // Resolución alta
    HIGH("Alta (1200 x 600)", 1200, 600, false),
    // Resolución muy alta
    VERY_HIGH("Muy alta (1600 x 800)", 1600, 800, false),
    // Todo el monitor
    FULL_SCREEN("Pantalla completa", 0, 0, true);

    // Texto que se muestra en el selector
    private final String label;
    // Ancho en píxeles del área de juego (0 en pantalla completa)
    private final int width;
    // Alto en píxeles del área de juego (0 en pantalla completa)
    private final int height;
    // Si ocupa todo el monitor
    private final boolean fullScreen;

    Resolution(String label, int width, int height, boolean fullScreen) {
        this.label = label;
        this.width = width;
        this.height = height;
        this.fullScreen = fullScreen;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    @Override
    public String toString() {
        return label;
    }
}
