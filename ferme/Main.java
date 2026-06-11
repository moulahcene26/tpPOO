package ferme;

import ferme.enums.*;
import ferme.models.*;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static Ferme ferme;

    // =========================================================================
    //  MAIN
    // =========================================================================

    public static void main(String[] args) {
        printMenu("Gestion Intelligente de la Ferme",
                "Bienvenue ! Commencez par creer votre ferme (option 1).");

        boolean running = true;
        while (running) {
            printMenu("Menu Principal",
                    "1. Creer / changer de ferme",
                    "2. Zones",
                    "3. Cultures",
                    "4. Animaux",
                    "5. Capteurs",
                    "6. Releves et production",
                    "7. Alertes",
                    "8. Tableaux de bord",
                    "9. Suspension / activation de zone",
                    "10. Charger donnees depuis fichier",
                    "0. Quitter");

            try {
                switch (readInt("Choix: ")) {
                    case 1: creerFerme();       break;
                    case 2: menuZones();        break;
                    case 3: menuCultures();     break;
                    case 4: menuAnimaux();      break;
                    case 5: menuCapteurs();     break;
                    case 6: menuReleves();      break;
                    case 7: menuAlertes();      break;
                    case 8: menuDashboard();    break;
                    case 9: menuSuspension();   break;
                    case 10: chargerDonneesGlobales(); break;
                    case 0: running = false;    break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }

        System.out.println("Au revoir !");
    }

    // =========================================================================
    //  1. FERME
    // =========================================================================

    private static void requireFerme() {
        if (ferme == null) {
            throw new IllegalStateException("Aucune ferme creee. Utilisez l'option 1 d'abord.");
        }
    }

    private static void creerFerme() {
        String nom = readRequiredString("Nom de la ferme: ");
        ferme = new Ferme(nom);
        System.out.println("Ferme << " + nom + " >> creee.");
    }

    // =========================================================================
    //  2. ZONES
    // =========================================================================

    private static void menuZones() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Zones",
                    "1. Vue d'ensemble des zones",
                    "2. Ajouter une zone de culture",
                    "3. Ajouter une zone d'elevage",
                    "4. Ajouter une zone aquacole",
                    "5. Ajouter une espece aquacole",
                    "6. Definir programme d'alimentation",
                    "7. Enregistrer une production",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: ferme.afficherVueEnsembleZones();     break;
                    case 2: ajouterZoneCulture();                 break;
                    case 3: ajouterZoneElevage();                 break;
                    case 4: ajouterZoneAquacole();                break;
                    case 5: ajouterEspeceAquacole();              break;
                    case 6: definirProgrammeAlimentation();       break;
                    case 7: enregistrerProduction();              break;
                    case 0: back = true;                          break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void ajouterZoneCulture() {
        String id  = readRequiredString("ID de la zone (ex: ZC01): ");
        String nom = readRequiredString("Nom de la zone: ");
        ferme.ajouterZone(new ZoneCulture(id, nom));
        System.out.println("Zone de culture ajoutee.");
    }

    private static void ajouterZoneElevage() {
        String id   = readRequiredString("ID de la zone (ex: ZE01): ");
        String nom  = readRequiredString("Nom de la zone: ");
        TypeElevage type = readEnum("Type d'elevage", TypeElevage.class);
        double lat  = readDouble("Latitude GPS: ");
        double lon  = readDouble("Longitude GPS: ");
        double surf = readDouble("Superficie (m2): ");
        ferme.ajouterZone(new ZoneElevage(id, nom, type, new PositionGPS(lat, lon), surf));
        System.out.println("Zone d'elevage ajoutee.");
    }

    private static void ajouterZoneAquacole() {
        String id  = readRequiredString("ID de la zone (ex: ZA01): ");
        String nom = readRequiredString("Nom de la zone: ");
        ferme.ajouterZone(new ZoneAquacole(id, nom));
        System.out.println("Zone aquacole ajoutee.");
    }

    private static void ajouterEspeceAquacole() {
        Zone z = choisirZone();
        if (!(z instanceof ZoneAquacole)) {
            throw new IllegalArgumentException("La zone choisie n'est pas aquacole.");
        }
        String nom = readRequiredString("Nom de l'espece: ");
        int    qte = readInt("Quantite initiale: ");
        ((ZoneAquacole) z).ajouterEspece(new EspeceAquacole(nom, qte));
        System.out.println("Espece ajoutee.");
    }

    private static void definirProgrammeAlimentation() {
        Zone z = choisirZone();
        String aliment = readRequiredString("Type d'aliment: ");
        double dose    = readDouble("Dose par repas (kg): ");
        int    freq    = readInt("Repas par jour: ");
        ProgrammeAlimentation pa = new ProgrammeAlimentation(aliment, dose, freq);
        if (z instanceof ZoneElevage) {
            ((ZoneElevage) z).setProgrammeAlimentation(pa);
        } else if (z instanceof ZoneAquacole) {
            ((ZoneAquacole) z).setProgrammeAlimentation(pa);
        } else {
            throw new IllegalArgumentException("Cette zone ne supporte pas un programme d'alimentation.");
        }
        System.out.println("Programme d'alimentation defini.");
    }

    // =========================================================================
    //  3. CULTURES
    // =========================================================================

    private static void menuCultures() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Cultures",
                    "1. Affecter une culture a une zone",
                    "2. Mettre a jour le stade de croissance",
                    "3. Afficher stades d'une zone",
                    "4. Rapport cultures par zone",
                    "5. Filtrer par famille de culture",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: affecterCulture();    break;
                    case 2: majStade();            break;
                    case 3: {
                        String id = readRequiredString("ID de la zone: ");
                        ferme.afficherStadeCroissance(id);
                        break;
                    }
                    case 4: {
                        String id = readRequiredString("ID de la zone: ");
                        ferme.genererRapportCulturesParZone(id);
                        break;
                    }
                    case 5: {
                        FamilleCulture fam = readEnum("Famille", FamilleCulture.class);
                        ferme.afficherCulturesParFamille(null, fam);
                        break;
                    }
                    case 0: back = true; break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void affecterCulture() {
        String idZone     = readRequiredString("ID de la zone: ");
        String nomCulture = readRequiredString("Nom de la culture: ");
        FamilleCulture fam = readEnum("Famille", FamilleCulture.class);
        String dateSemis   = readDate("Date de semis   (AAAA-MM-JJ): ");
        String dateRecolte = readDate("Date de recolte (AAAA-MM-JJ): ");
        System.out.println("-- Exigences pedologiques --");
        double[] ph = readDoubleRange("pH minimum: ", "pH maximum: ", "pH");
        double[] hum = readDoubleRange("Humidite min (%): ", "Humidite max (%): ", "humidite");
        ferme.affecterCultureAZone(idZone,
                new Culture(nomCulture, fam, dateSemis, dateRecolte,
                new ExigencesPedologiques(ph[0], ph[1], hum[0], hum[1])));
        System.out.println("Culture affectee.");
    }

    private static void majStade() {
        String idZone = readRequiredString("ID de la zone: ");
        int    index  = readInt("Index de la culture (0 = premiere): ");
        StadeCroissance stade = readEnum("Stade de croissance", StadeCroissance.class);
        ferme.mettreAJourStade(idZone, index, stade);
        System.out.println("Stade mis a jour.");
    }

    // =========================================================================
    //  4. ANIMAUX
    // =========================================================================

    private static void menuAnimaux() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Animaux",
                    "1. Affecter un animal a une zone",
                    "2. Mettre a jour l'etat de sante",
                    "3. Enregistrer un evenement sanitaire",
                    "4. Enregistrer evolution du poids",
                    "5. Afficher programme d'alimentation",
                    "6. Filtrer par etat de sante",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: affecterAnimal();           break;
                    case 2: majSante();                 break;
                    case 3: enregistrerEvtSanitaire();  break;
                    case 4: enregistrerPoids();         break;
                    case 5: {
                        String id = readRequiredString("ID de la zone d'elevage: ");
                        ferme.afficherProgrammeAlimentation(id);
                        break;
                    }
                    case 6: {
                        EtatSante etat = readEnum("Etat de sante", EtatSante.class);
                        ferme.afficherAnimauxParEtatSante(null, etat);
                        break;
                    }
                    case 0: back = true; break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void affecterAnimal() {
        String idZone = readRequiredString("ID de la zone d'elevage: ");
        int    id     = readInt("ID de l'animal: ");
        String espece = readRequiredString("Espece (ex: Vache): ");
        TypeElevage type = readEnum("Type d'elevage", TypeElevage.class);
        int    age   = readInt("Age (annees): ");
        double poids = readDouble("Poids (kg): ");
        EtatSante etat = readEnum("Etat de sante", EtatSante.class);
        Animal a = new Animal(id, espece, type, age, poids);
        a.setEtatSante(etat);
        ferme.affecterAnimalAZone(idZone, a);
        System.out.println("Animal #" + id + " affecte.");
    }

    private static void majSante() {
        int id = readInt("ID de l'animal: ");
        EtatSante etat = readEnum("Nouvel etat de sante", EtatSante.class);
        ferme.mettreAJourSante(id, etat);
        System.out.println("Etat de sante mis a jour.");
    }

    private static void enregistrerEvtSanitaire() {
        int    id   = readInt("ID de l'animal: ");
        String desc = readRequiredString("Description: ");
        String date = readDate("Date (AAAA-MM-JJ): ");
        ferme.enregistrerEvenementSanitaire(id, desc, date);
        System.out.println("Evenement sanitaire enregistre.");
    }

    private static void enregistrerPoids() {
        int    id    = readInt("ID de l'animal: ");
        double poids = readDouble("Poids actuel (kg): ");
        String date  = readDate("Date (AAAA-MM-JJ): ");
        ferme.enregistrerEvolutionPoids(id, poids, date);
        System.out.println("Poids enregistre.");
    }

    // =========================================================================
    //  5. CAPTEURS
    // =========================================================================

    private static void menuCapteurs() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Capteurs",
                    "1. Ajouter capteur environnemental",
                    "2. Ajouter capteur de sol",
                    "3. Ajouter capteur biometrique",
                    "4. Ajouter capteur GPS",
                    "5. Ajouter capteur eau",
                    "6. Charger capteurs depuis fichier",
                    "7. Changer statut d'un capteur",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: ajouterCapteurEnv(); break;
                    case 2: ajouterCapteurSol(); break;
                    case 3: ajouterCapteurBio(); break;
                    case 4: ajouterCapteurGPS(); break;
                    case 5: ajouterCapteurEau(); break;
                    case 6: chargerCapteursFichiers(); break;
                    case 7: changerStatutCapteur(); break;
                    case 0: back = true;         break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void ajouterCapteurEnv() {
        String id     = readRequiredString("ID capteur (ex: ENV01): ");
        String idZone = readRequiredString("ID de la zone: ");
        TypeCapteurEnv type = readEnum("Type", TypeCapteurEnv.class);
        double min = readDouble("Seuil minimum: ");
        double max = readDouble("Seuil maximum: ");
        ferme.ajouterCapteur(new CapteurEnvironnemental(id, idZone, min, max, type));
        System.out.println("Capteur environnemental ajoute.");
    }

    private static void ajouterCapteurSol() {
        String id     = readRequiredString("ID capteur (ex: SOL01): ");
        String idZone = readRequiredString("ID de la zone: ");
        TypeCapteurSol type = readEnum("Type", TypeCapteurSol.class);
        double min = readDouble("Seuil minimum: ");
        double max = readDouble("Seuil maximum: ");
        ferme.ajouterCapteur(new CapteurSol(id, idZone, min, max, type));
        System.out.println("Capteur de sol ajoute.");
    }

    private static void ajouterCapteurBio() {
        String id       = readRequiredString("ID capteur (ex: BIO01): ");
        String idZone   = readRequiredString("ID de la zone: ");
        TypeCapteurBio type = readEnum("Type", TypeCapteurBio.class);
        double min      = readDouble("Seuil minimum: ");
        double max      = readDouble("Seuil maximum: ");
        int    idAnimal = readInt("ID de l'animal lie: ");
        ferme.ajouterCapteur(new CapteurBiometrique(id, idZone, min, max, type, idAnimal));
        System.out.println("Capteur biometrique ajoute.");
    }

    private static void ajouterCapteurGPS() {
        String id       = readRequiredString("ID capteur (ex: GPS01): ");
        String idZone   = readRequiredString("ID de la zone: ");
        int    idAnimal = readInt("ID de l'animal lie: ");
        double lat      = readDouble("Latitude de reference: ");
        double lon      = readDouble("Longitude de reference: ");
        double rayon    = readDouble("Rayon du perimetre (m): ");
        ferme.ajouterCapteur(new CapteurGPS(id, idZone, idAnimal, lat, lon, rayon));
        System.out.println("Capteur GPS ajoute.");
    }

    private static void ajouterCapteurEau() {
        String id     = readRequiredString("ID capteur (ex: EAU01): ");
        String idZone = readRequiredString("ID de la zone aquacole: ");
        TypeCapteurEau type = readEnum("Type", TypeCapteurEau.class);
        double min = readDouble("Seuil minimum: ");
        double max = readDouble("Seuil maximum: ");
        ferme.ajouterCapteur(new CapteurEau(id, idZone, min, max, type));
        System.out.println("Capteur eau ajoute.");
    }

    private static void chargerCapteursFichiers() {
        int nb = readInt("Nombre de fichiers capteurs: ");
        String[] chemins = new String[nb];
        for (int i = 0; i < nb; i++) {
            chemins[i] = readRequiredString("Fichier capteurs " + (i + 1) + ": ");
        }
        int total = ferme.chargerCapteursDepuisFichiers(chemins);
        System.out.println("Capteurs charges : " + total);
    }

    private static void changerStatutCapteur() {
        String codeCapteur = readRequiredString("Code du capteur: ");
        StatutCapteur statut = readEnum("Nouveau statut", StatutCapteur.class);
        ferme.changerStatutCapteur(codeCapteur, statut);
    }

    // =========================================================================
    //  6. RELEVES & PRODUCTION
    // =========================================================================

    private static void menuReleves() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Releves et production",
                    "1. Saisir un releve numerique",
                    "2. Charger releves depuis fichiers",
                    "3. Consulter production par plage de dates",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: saisirReleve();        break;
                    case 2: chargerFichiers();     break;
                    case 3: consulterProduction(); break;
                    case 0: back = true;           break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void saisirReleve() {
        String idCapteur = readRequiredString("ID du capteur: ");
        double valeur    = readDouble("Valeur mesuree: ");
        String unite     = readRequiredString("Unite (ex: C, %, mg/L): ");
        String date      = readDateReleve("Date (AAAA-MM-JJ-HHh): ");
        ferme.enregistrerReleve(idCapteur, new ReleveNumerique(valeur, unite, date, idCapteur));
        System.out.println("Releve enregistre.");
    }

    private static void chargerFichiers() {
        int nb = readInt("Nombre de fichiers: ");
        String[] chemins = new String[nb];
        for (int i = 0; i < nb; i++) {
            chemins[i] = readRequiredString("Fichier " + (i + 1) + ": ");
        }
        int total = ferme.chargerRelevesDepuisFichiers(chemins);
        System.out.println("Releves charges : " + total);
    }

    private static void enregistrerProduction() {
        String idZone = readRequiredString("ID de la zone: ");
        double qte    = readDouble("Quantite produite: ");
        String date   = readDate("Date (AAAA-MM-JJ): ");
        ferme.enregistrerProduction(idZone, qte, date);
        System.out.println("Production enregistree.");
    }

    private static void consulterProduction() {
        String idZone = readRequiredString("ID de la zone: ");
        String debut  = readDate("Date debut (AAAA-MM-JJ): ");
        String fin    = readDate("Date fin   (AAAA-MM-JJ): ");
        ferme.afficherProductionParPlageDate(idZone, debut, fin);
    }

    // =========================================================================
    //  7. ALERTES
    // =========================================================================

    private static void menuAlertes() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Alertes",
                    "1. Panneau d'alertes (toutes)",
                    "2. Alertes actives seulement",
                    "3. Trier par gravite",
                    "4. Acquitter une alerte",
                    "5. Historique filtre par zone",
                    "6. Historique filtre par gravite",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: ferme.afficherPanneauAlertes();  break;
                    case 2: ferme.afficherAlertesActives();  break;
                    case 3: {
                        ferme.afficherAlertesTrierParGravite();
                        break;
                    }
                    case 4: {
                        int id = readInt("ID de l'alerte: ");
                        ferme.acquitterAlerte(id);
                        break;
                    }
                    case 5: {
                        String idZone = readRequiredString("ID de la zone: ");
                        ferme.consulterHistoriqueAlertes(idZone, null, null, null, null);
                        break;
                    }
                    case 6: {
                        NiveauGravite g = readEnum("Niveau de gravite", NiveauGravite.class);
                        ferme.consulterHistoriqueAlertes(null, g, null, null, null);
                        break;
                    }
                    case 0: back = true; break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    // =========================================================================
    //  8. TABLEAUX DE BORD
    // =========================================================================

    private static void menuDashboard() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Tableaux de bord",
                    "1. Tableau de bord d'une zone",
                    "2. Details d'une zone",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: {
                        String id = readRequiredString("ID de la zone: ");
                        ferme.afficherTableauDeBord(id);
                        break;
                    }
                    case 2: {
                        Zone z = choisirZone();
                        z.afficherDetails();
                        break;
                    }
                    case 0: back = true; break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    // =========================================================================
    //  9. SUSPENSION / ACTIVATION
    // =========================================================================

    private static void menuSuspension() {
        requireFerme();
        boolean back = false;
        while (!back) {
            printMenu("Suspension / Activation",
                    "1. Suspendre une zone",
                    "2. Activer une zone",
                    "0. Retour");

            try {
                switch (readInt("Choix: ")) {
                    case 1: {
                        String id = readRequiredString("ID de la zone a suspendre: ");
                        ferme.suspendreZone(id);
                        System.out.println("Zone suspendue.");
                        break;
                    }
                    case 2: {
                        String id = readRequiredString("ID de la zone a activer: ");
                        ferme.activerZone(id);
                        System.out.println("Zone activee.");
                        break;
                    }
                    case 0: back = true; break;
                    default: System.out.println("Choix inconnu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void chargerDonneesGlobales() {
        requireFerme();
        String fichier = readRequiredString("Chemin du fichier de donnees: ");
        int total = ImporteurDonnees.chargerDepuisFichier(ferme, fichier);
        System.out.println("Donnees chargees : " + total);
    }

    // =========================================================================
    //  HELPERS — NAVIGATION
    // =========================================================================

    private static Zone choisirZone() {
        String id = readRequiredString("ID de la zone: ");
        Zone z = ferme.getZoneById(id);
        if (z == null) {
            throw new IllegalArgumentException("Zone introuvable : " + id);
        }
        return z;
    }

    // =========================================================================
    //  HELPERS — SAISIE
    // =========================================================================

    private static String readRequiredString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Ce champ est obligatoire.");
        }
    }

    private static String readOptionalString(String label) {
        System.out.print(label + " (vide = tous): ");
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    private static String readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return ValidationUtils.validerDateSimple(value, "Date");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String readDateReleve(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return ValidationUtils.validerDateReleve(value, "Date de relevé");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static double[] readDoubleRange(String promptMin, String promptMax, String label) {
        while (true) {
            double min = readDouble(promptMin);
            double max = readDouble(promptMax);
            try {
                ValidationUtils.validerBornes(min, max, label + " minimum", label + " maximum");
                return new double[] { min, max };
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entier attendu.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Nombre decimal attendu (ex: 36.5).");
            }
        }
    }

    /** Affiche les choix d'un enum avec un numéro à sélectionner. */
    private static <T extends Enum<T>> T readEnum(String label, Class<T> enumType) {
        while (true) {
            printEnumChoices(label, enumType);
            try {
                int choix = Integer.parseInt(scanner.nextLine().trim());
                T[] values = enumType.getEnumConstants();
                if (choix >= 1 && choix <= values.length) {
                    return values[choix - 1];
                }
                System.out.println("Choix invalide.");
            } catch (IllegalArgumentException e) {
                System.out.println("Nombre attendu.");
            }
        }
    }

    private static <T extends Enum<T>> void printEnumChoices(String label, Class<T> enumType) {
        T[] values = enumType.getEnumConstants();
        System.out.println(label + " :");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  [" + (i + 1) + "] " + values[i].name());
        }
        System.out.print("Votre choix: ");
    }

    // =========================================================================
    //  HELPERS — AFFICHAGE
    // =========================================================================

    private static void printMenu(String title, String... options) {
        int width = Math.max(38, title.length() + 6);
        String border = repeat("=", width);
        System.out.println();
        System.out.println(border);
        System.out.println(center(title, width));
        System.out.println(border);
        for (String opt : options) {
            System.out.println(opt);
        }
        System.out.println(border);
    }

    private static String center(String text, int width) {
        int left  = Math.max(0, (width - text.length()) / 2);
        int right = Math.max(0, width - text.length() - left);
        return repeat(" ", left) + text + repeat(" ", right);
    }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }
}