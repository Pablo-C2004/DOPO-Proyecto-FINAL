package domain;

/**
 * Estados Unidos — Ilusión (daño 30).
 */
public class UnitedStates extends GameCharacter {

    public static final String COUNTRY = "Estados Unidos";

    @Override
    public String getCountry() {
        return COUNTRY;
    }

    @Override
    public int getShotDamage() {
        return 30;
    }

    @Override
    public PowerShot createPowerShot() {
        return new IllusionShot(getShotDamage());
    }
}
