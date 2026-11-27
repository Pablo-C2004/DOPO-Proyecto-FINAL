package domain;

/**
 * Camerún — Rayo (daño 60).
 */
public class Cameroon extends GameCharacter {

    public static final String COUNTRY = "Camerún";

    @Override
    public String getCountry() {
        return COUNTRY;
    }

    @Override
    public int getShotDamage() {
        return 60;
    }

    @Override
    public PowerShot createPowerShot() {
        return new LightningShot(getShotDamage());
    }
}
