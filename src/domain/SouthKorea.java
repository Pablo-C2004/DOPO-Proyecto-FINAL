package domain;

/**
 * Corea del Sur — Aura Azul (daño 40).
 */
public class SouthKorea extends GameCharacter {
    public static final String COUNTRY = "Corea del Sur";

    @Override
    public String getCountry() {
        return COUNTRY;
    }

    @Override
    public int getShotDamage() {
        return 40;
    }

    @Override
    public PowerShot createPowerShot() {
        return new BlueAuraShot(getShotDamage());
    }
}
