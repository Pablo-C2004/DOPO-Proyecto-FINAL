package domain;

// Escribe los registros del log en un archivo
import java.util.logging.FileHandler;
// Nivel de gravedad de un registro (SEVERE, INFO...)
import java.util.logging.Level;
// Objeto que registra los mensajes del log
import java.util.logging.Logger;
// Formato de texto legible para el archivo de log
import java.util.logging.SimpleFormatter;

/**
 * Registro de errores para los programadores.
 * Los errores inesperados se guardan en el archivo DOPOSoccer.log.
 */
public class Log {

    // Nombre del logger y del archivo de log
    public static final String NAME = "DOPOSoccer";

    private Log() {
    }

    /**
     * Registra una excepción en el archivo de log.
     * @param e excepción a registrar
     */
    public static void record(Exception e) {
        try {
            Logger logger = Logger.getLogger(NAME);
            logger.setUseParentHandlers(false);
            FileHandler file = new FileHandler(NAME + ".log", true);
            file.setFormatter(new SimpleFormatter());
            logger.addHandler(file);
            logger.log(Level.SEVERE, e.toString(), e);
            file.close();
            logger.removeHandler(file);
        } catch (Exception oe) {
            oe.printStackTrace();
        }
    }
}
