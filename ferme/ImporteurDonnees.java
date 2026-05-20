package ferme;

import ferme.enums.*;
import ferme.models.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class ImporteurDonnees {

    private ImporteurDonnees() {
    }

    public static int chargerDepuisFichier(Ferme ferme, String cheminFichier) {
        int nbLus = 0;
        int nbAcceptes = 0;
        int nbErreurs = 0;

        System.out.println("\n--- Chargement des donnees globales depuis : " + cheminFichier + " ---");

        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            int numeroLigne = 0;

            while ((ligne = reader.readLine()) != null) {
                numeroLigne++;
                String brute = ligne.trim();

                if (brute.isEmpty() || brute.startsWith("#")) {
                    continue;
                }

                nbLus++;
                String[] morceaux = brute.split(";");
                String type = morceaux[0].trim().toUpperCase();

                try {
                    boolean ok = traiterLigne(ferme, type, morceaux);
                    if (ok) {
                        nbAcceptes++;
                    } else {
                        System.out.println("  Ligne " + numeroLigne + " ignorée (type inconnu) : " + brute);
                        nbErreurs++;
                    }
                } catch (RuntimeException e) {
                    System.out.println("  Ligne " + numeroLigne + " ignorée (" + e.getMessage() + ") : " + brute);
                    nbErreurs++;
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur de lecture du fichier \"" + cheminFichier + "\" : " + e.getMessage());
            return 0;
        }

        System.out.println("  Lignes de donnees : " + nbLus
                + " | Entites traitees : " + nbAcceptes
                + " | Erreurs : " + nbErreurs);
        return nbAcceptes;
    }

    private static boolean traiterLigne(Ferme ferme, String type, String[] morceaux) {
        if (morceaux.length == 0) {
            return false;
        }

        if ("FERME".equals(type)) {
            if (morceaux.length < 2) {
                throw new IllegalArgumentException("nom de ferme manquant");
            }
            System.out.println("  Ferme definie : " + morceaux[1].trim());
            return true;
        }

        if ("ZONE_CULTURE".equals(type)) {
            verifierLongueur(morceaux, 3, "zone de culture incomplete");
            return ferme.ajouterZone(new ZoneCulture(morceaux[1].trim(), morceaux[2].trim()));
        }

        if ("ZONE_ELEVAGE".equals(type)) {
            verifierLongueur(morceaux, 7, "zone d'elevage incomplete");
            TypeElevage typeElevage = TypeElevage.valueOf(morceaux[3].trim().toUpperCase());
            double latitude = Double.parseDouble(morceaux[4].trim());
            double longitude = Double.parseDouble(morceaux[5].trim());
            double surface = Double.parseDouble(morceaux[6].trim());
            return ferme.ajouterZone(new ZoneElevage(morceaux[1].trim(), morceaux[2].trim(), typeElevage,
                    new PositionGPS(latitude, longitude), surface));
        }

        if ("ZONE_AQUACOLE".equals(type)) {
            verifierLongueur(morceaux, 3, "zone aquacole incomplete");
            return ferme.ajouterZone(new ZoneAquacole(morceaux[1].trim(), morceaux[2].trim()));
        }

        if ("CULTURE".equals(type)) {
            verifierLongueur(morceaux, 10, "culture incomplete");
            String codeZone = morceaux[1].trim();
            String nom = morceaux[2].trim();
            FamilleCulture famille = FamilleCulture.valueOf(morceaux[3].trim().toUpperCase());
            String datePlantation = ValidationUtils.validerDateSimple(morceaux[4].trim(), "date de plantation");
            String dateRecolte = ValidationUtils.validerDateSimple(morceaux[5].trim(), "date de récolte prévue");
            double phMin = Double.parseDouble(morceaux[6].trim());
            double phMax = Double.parseDouble(morceaux[7].trim());
            int humMin = Integer.parseInt(morceaux[8].trim());
            int humMax = Integer.parseInt(morceaux[9].trim());
            Culture culture = new Culture(nom, famille, datePlantation, dateRecolte,
                    new ExigencesPedologiques(phMin, phMax, humMin, humMax));
            return ferme.affecterCultureAZone(codeZone, culture);
        }

        if ("ANIMAL".equals(type)) {
            verifierLongueur(morceaux, 8, "animal incomplet");
            String codeZone = morceaux[1].trim();
            int numero = Integer.parseInt(morceaux[2].trim());
            String espece = morceaux[3].trim();
            TypeElevage typeElevage = TypeElevage.valueOf(morceaux[4].trim().toUpperCase());
            int age = Integer.parseInt(morceaux[5].trim());
            double poids = Double.parseDouble(morceaux[6].trim());
            EtatSante etat = EtatSante.valueOf(morceaux[7].trim().toUpperCase());
            Animal animal = new Animal(numero, espece, typeElevage, age, poids);
            animal.setEtatSante(etat);
            return ferme.affecterAnimalAZone(codeZone, animal);
        }

        if ("CAPTEUR_ENV".equals(type)) {
            verifierLongueur(morceaux, 6, "capteur environnemental incomplet");
            TypeCapteurEnv sousType = TypeCapteurEnv.valueOf(morceaux[3].trim().toUpperCase());
            double seuilMin = Double.parseDouble(morceaux[4].trim());
            double seuilMax = Double.parseDouble(morceaux[5].trim());
            return ferme.ajouterCapteur(new CapteurEnvironnemental(morceaux[1].trim(), morceaux[2].trim(),
                    seuilMin, seuilMax, sousType));
        }

        if ("CAPTEUR_SOL".equals(type)) {
            verifierLongueur(morceaux, 6, "capteur de sol incomplet");
            TypeCapteurSol sousType = TypeCapteurSol.valueOf(morceaux[3].trim().toUpperCase());
            double seuilMin = Double.parseDouble(morceaux[4].trim());
            double seuilMax = Double.parseDouble(morceaux[5].trim());
            return ferme.ajouterCapteur(new CapteurSol(morceaux[1].trim(), morceaux[2].trim(),
                    seuilMin, seuilMax, sousType));
        }

        if ("CAPTEUR_BIO".equals(type)) {
            verifierLongueur(morceaux, 7, "capteur biometrique incomplet");
            TypeCapteurBio sousType = TypeCapteurBio.valueOf(morceaux[3].trim().toUpperCase());
            double seuilMin = Double.parseDouble(morceaux[4].trim());
            double seuilMax = Double.parseDouble(morceaux[5].trim());
            int numeroAnimal = Integer.parseInt(morceaux[6].trim());
            return ferme.ajouterCapteur(new CapteurBiometrique(morceaux[1].trim(), morceaux[2].trim(),
                    seuilMin, seuilMax, sousType, numeroAnimal));
        }

        if ("CAPTEUR_GPS".equals(type)) {
            verifierLongueur(morceaux, 7, "capteur GPS incomplet");
            int numeroAnimal = Integer.parseInt(morceaux[3].trim());
            double latitude = Double.parseDouble(morceaux[4].trim());
            double longitude = Double.parseDouble(morceaux[5].trim());
            double rayon = Double.parseDouble(morceaux[6].trim());
            return ferme.ajouterCapteur(new CapteurGPS(morceaux[1].trim(), morceaux[2].trim(), numeroAnimal,
                    latitude, longitude, rayon));
        }

        if ("CAPTEUR_EAU".equals(type)) {
            verifierLongueur(morceaux, 6, "capteur d'eau incomplet");
            TypeCapteurEau sousType = TypeCapteurEau.valueOf(morceaux[3].trim().toUpperCase());
            double seuilMin = Double.parseDouble(morceaux[4].trim());
            double seuilMax = Double.parseDouble(morceaux[5].trim());
            return ferme.ajouterCapteur(new CapteurEau(morceaux[1].trim(), morceaux[2].trim(),
                    seuilMin, seuilMax, sousType));
        }

        return false;
    }

    private static void verifierLongueur(String[] morceaux, int minimum, String message) {
        if (morceaux.length < minimum) {
            throw new IllegalArgumentException(message);
        }
    }
}
