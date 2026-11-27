package domain;

/**
 * Nigeria — Tornado (daño 40).
 */
public class Nigeria extends GameCharacter {

    public static final String COUNTRY = "Nigeria";

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
        return new TornadoShot(getShotDamage());
    }
}
