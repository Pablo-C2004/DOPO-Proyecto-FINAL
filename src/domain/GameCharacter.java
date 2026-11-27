package domain;

// para poder guardar el objeto en un archivo
import java.io.Serializable;

/**
 * Personaje (país) que escoge un jugador. Cada personaje tiene un tiro de
 * poder único. Para agregar un personaje nuevo se extiende esta clase y se
 * registra en CharacterFactory.
 */
public abstract class GameCharacter implements Serializable {

    // Versión de la clase al guardar y abrir partidas
    private static final long serialVersionUID = 1L;

    public abstract String getCountry();

    /**
     * Daño base del tiro (DañoTiro, ya multiplicado × 5).
     */
    public abstract int getShotDamage();

    /**
     * Crea un tiro de poder nuevo de este personaje.
     */
    public abstract PowerShot createPowerShot();

    public String getPowerName() {
        return createPowerShot().getName();
    }

    @Override
    public String toString() {
        return getCountry();
    }
}
