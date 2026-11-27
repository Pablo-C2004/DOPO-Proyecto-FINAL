package presentation;

// Registro de errores para los programadores
import domain.Log;

// Lee archivos de imagen (PNG) del disco
import javax.imageio.ImageIO;
// Imagen en memoria que se puede dibujar y recortar
import java.awt.image.BufferedImage;
// Archivo o carpeta en el disco
import java.io.File;
// Excepción para indicar que una imagen no existe
import java.io.FileNotFoundException;
// Lista dinámica basada en arreglo
import java.util.ArrayList;
// Utilidades de colecciones (lista no modificable)
import java.util.Collections;
// Mapa nombre → valor basado en tabla hash
import java.util.HashMap;
// Interfaz de lista
import java.util.List;
// Interfaz de mapa nombre → valor
import java.util.Map;

/**
 * Carga las imágenes del juego desde la carpeta images/ (gráficos tomados de OpenGameArt).
 * Si una imagen no existe o no se puede leer, se registra en el log y el juego
 * usa los dibujos simples de respaldo en su lugar.
 */
public final class Sprites {

    // Nombre de la carpeta donde están las imágenes
    public static final String FOLDER = "images";

    // Animación del jugador 1 quieto (tira de cuadros cuadrados)
    public static final String PLAYER1_IDLE = "player1_idle.png";
    // Animación del jugador 1 caminando
    public static final String PLAYER1_WALK = "player1_walk.png";
    // Animación del jugador 2 quieto
    public static final String PLAYER2_IDLE = "player2_idle.png";
    // Animación del jugador 2 caminando
    public static final String PLAYER2_WALK = "player2_walk.png";
    // Pie que se dibuja al patear
    public static final String FOOT = "foot.png";
    // Balón
    public static final String BALL = "ball.png";
    // Arco izquierdo
    public static final String GOAL_LEFT = "goal_left.png";
    // Arco derecho
    public static final String GOAL_RIGHT = "goal_right.png";
    // Césped (se repite a lo largo de la cancha)
    public static final String GRASS = "grass.png";
    // Fondo del escenario Pradera
    public static final String BACKGROUND_MEADOW = "background_meadow.png";
    // Fondo del escenario Cueva
    public static final String BACKGROUND_CAVE = "background_cave.png";
    // Fondo del escenario Desierto
    public static final String BACKGROUND_DESERT = "background_desert.png";
    // Todas las imágenes que el juego intenta cargar
    public static final String[] FILES = {
        PLAYER1_IDLE, PLAYER1_WALK, PLAYER2_IDLE, PLAYER2_WALK,
        FOOT, BALL, GOAL_LEFT, GOAL_RIGHT, GRASS,
        BACKGROUND_MEADOW, BACKGROUND_CAVE, BACKGROUND_DESERT
    };

    // Milisegundos por cuadro de la animación de caminar
    public static final int WALK_FRAME_MS = 10;
    // Milisegundos por cuadro de la animación quieto
    public static final int IDLE_FRAME_MS = 30;

    // Imágenes cargadas, por nombre de archivo
    private static final Map<String, BufferedImage> IMAGES = new HashMap<>();
    // Cuadros de cada animación ya recortados, por nombre de archivo
    private static final Map<String, BufferedImage[]> FRAMES = new HashMap<>();
    // Nombres de las imágenes que no se pudieron cargar
    private static final List<String> MISSING = new ArrayList<>();
    // Indica si ya se intentó cargar las imágenes
    private static boolean loaded;

    private Sprites() {
    }

    /**
     * Carga todas las imágenes (solo la primera vez que se llama).
     */
    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        File folder = findFolder();
        for (String name : FILES) {
            File file = new File(folder, name);
            try {
                if (!file.isFile()) {
                    throw new FileNotFoundException(file.getAbsolutePath());
                }
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    throw new FileNotFoundException("Formato no soportado: " + file.getAbsolutePath());
                }
                IMAGES.put(name, image);
            } catch (Exception e) {
                MISSING.add(name);
                Log.record(e);
            }
        }
    }

    /**
     * Busca la carpeta de imágenes primero en la carpeta actual y luego
     * junto a las clases compiladas
     */
    private static File findFolder() {
        List<File> candidates = new ArrayList<>();
        candidates.add(new File(FOLDER));
        try {
            File code = new File(Sprites.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            candidates.add(new File(code, FOLDER));
            if (code.getParentFile() != null) {
                candidates.add(new File(code.getParentFile(), FOLDER));
            }
        } catch (Exception e) {
            Log.record(e);
        }
        for (File candidate : candidates) {
            if (candidate.isDirectory()) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    /**
     * @return la imagen o null si no se pudo cargar
     */
    public static BufferedImage get(String name) {
        load();
        return IMAGES.get(name);
    }

    /**
     * Recorta una tira de animación en cuadros cuadrados (alto × alto).
     * @return los cuadros o null si la imagen no se pudo cargar
     */
    public static BufferedImage[] frames(String name) {
        BufferedImage sheet = get(name);
        if (sheet == null) {
            return null;
        }
        return FRAMES.computeIfAbsent(name, n -> {
            int size = sheet.getHeight();
            int count = Math.max(1, sheet.getWidth() / size);
            BufferedImage[] frames = new BufferedImage[count];
            for (int i = 0; i < count; i++) {
                frames[i] = sheet.getSubimage(i * size, 0, size, size);
            }
            return frames;
        });
    }

    /**
     * Cuadro de una animación que corresponde a un instante
     * @param timeMs tiempo del juego en milisegundos
     * @param frameMs milisegundos que dura cada cuadro
     */
    public static BufferedImage frameAt(String name, long timeMs, int frameMs) {
        BufferedImage[] frames = frames(name);
        if (frames == null) {
            return null;
        }
        int index = (int) ((timeMs / frameMs) % frames.length);
        return frames[index];
    }

    /**
     * @return los nombres de las imágenes que no se encontraron
     */
    public static List<String> getMissing() {
        load();
        return Collections.unmodifiableList(MISSING);
    }
}
