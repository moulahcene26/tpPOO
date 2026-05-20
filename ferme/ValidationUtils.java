package ferme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class ValidationUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private ValidationUtils() {
    }

    public static String validerDateSimple(String valeur, String libelle) {
        if (valeur == null) {
            throw new IllegalArgumentException(libelle + " manquante.");
        }
        String nettoyee = valeur.trim();
        try {
            LocalDate.parse(nettoyee, DATE_FORMAT);
            return nettoyee;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(libelle + " invalide. Format attendu: AAAA-MM-JJ.");
        }
    }

    public static String validerDateReleve(String valeur, String libelle) {
        if (valeur == null) {
            throw new IllegalArgumentException(libelle + " manquante.");
        }
        String nettoyee = valeur.trim();
        if (!nettoyee.matches("^\\d{4}-\\d{2}-\\d{2}-\\d{2}h$")) {
            throw new IllegalArgumentException(libelle + " invalide. Format attendu: AAAA-MM-JJ-HHh.");
        }
        String datePartie = nettoyee.substring(0, 10);
        LocalDate.parse(datePartie, DATE_FORMAT);
        int heure = Integer.parseInt(nettoyee.substring(11, 13));
        if (heure < 0 || heure > 23) {
            throw new IllegalArgumentException(libelle + " invalide. Heure attendue entre 00h et 23h.");
        }
        return nettoyee;
    }

    public static void validerBornes(double min, double max, String libelleMin, String libelleMax) {
        if (max < min) {
            throw new IllegalArgumentException(libelleMax + " doit être supérieur ou égal à " + libelleMin + ".");
        }
    }
}
