package domain;

/**
 * Crea personajes a partir del nombre del país.
 * Punto único de registro para nuevos personajes.
 */
public final class CharacterFactory {

    // Países disponibles para escoger
    public static final String[] COUNTRIES = {
        SouthKorea.COUNTRY, Cameroon.COUNTRY, Nigeria.COUNTRY, UnitedStates.COUNTRY
    };

    private CharacterFactory() {
    }

    public static GameCharacter create(String country) throws DOPOSoccerException {
        if (country == null) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_CHARACTER);
        }
        switch (country) {
            case SouthKorea.COUNTRY: return new SouthKorea();
            case Cameroon.COUNTRY: return new Cameroon();
            case Nigeria.COUNTRY: return new Nigeria();
            case UnitedStates.COUNTRY: return new UnitedStates();
            default: throw new DOPOSoccerException(DOPOSoccerException.INVALID_CHARACTER);
        }
    }
}
