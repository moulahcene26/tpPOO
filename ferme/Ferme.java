package ferme;

import ferme.enums.*;
import ferme.interfaces.*;
import ferme.models.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ferme implements IGestionZones, IGestionCultures, IGestionAnimaux, IGestionCapteurs, IGestionAlertes {

    public static final int MAX_ALERTES = 500;

    private String nomFerme;
    private Map<String, Zone> zones;
    private Map<String, Capteur> capteurs;
    private List<Alerte> alertes;

    private static final String ANSI_RESET = "[0m";
    private static final String ANSI_VERT = "[32m";
    private static final String ANSI_JAUNE = "[33m";
    private static final String ANSI_ROUGE = "[31m";

    public Ferme(String nomFerme) {
        this.nomFerme = nomFerme;
        this.zones = new HashMap<>();
        this.capteurs = new HashMap<>();
        this.alertes = new ArrayList<>();
    }

    public String getNomFerme() { return nomFerme; }

    // ======================== IGestionZones ========================

    public boolean ajouterZone(Zone zone) {
        if (rechercherZone(zone.getCode()) != null) {
            System.out.println("Erreur : une zone avec le code " + zone.getCode() + " existe déjà.");
            return false;
        }
        zones.put(zone.getCode(), zone);
        System.out.println("Zone ajoutée : " + zone);
        return true;
    }

    public boolean modifierZone(String codeZone, String nouveauNom) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return false;
        }
        zone.setNom(nouveauNom);
        System.out.println("Zone " + codeZone + " renommée en \"" + nouveauNom + "\".");
        return true;
    }

    public boolean activerZone(String codeZone) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return false;
        }
        zone.activer();
        for (Capteur c : capteurs.values()) {
            if (c.getCodeZone().equals(codeZone)) {
                c.reactiver();
            }
        }
        System.out.println("Zone " + codeZone + " activée. Capteurs associés réactivés.");
        return true;
    }

    public boolean suspendreZone(String codeZone) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return false;
        }
        zone.suspendre();
        for (Capteur c : capteurs.values()) {
            if (c.getCodeZone().equals(codeZone)) {
                c.suspendre();
            }
        }
        System.out.println("Zone " + codeZone + " suspendue. Capteurs associés suspendus.");
        return true;
    }

    public Zone rechercherZone(String codeZone) {
        return zones.get(codeZone);
    }

    // Compatibilité : méthode d'accès par ID attendue par certains appels (Main.java)
    public Zone getZoneById(String id) {
        return rechercherZone(id);
    }

    public boolean affecterCultureAZone(String codeZone, Culture culture) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return false;
        }
        if (!(zone instanceof ZoneCulture)) {
            System.out.println("Erreur : la zone " + codeZone + " n'est pas une zone de culture.");
            return false;
        }
        boolean ok = ((ZoneCulture) zone).ajouterCulture(culture);
        if (ok) System.out.println("Culture \"" + culture.getNom() + "\" affectée à la zone " + codeZone + ".");
        return ok;
    }

    public boolean affecterAnimalAZone(String codeZone, Animal animal) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return false;
        }
        if (!(zone instanceof ZoneElevage)) {
            System.out.println("Erreur : la zone " + codeZone + " n'est pas une zone d'élevage.");
            return false;
        }
        if (rechercherAnimal(animal.getNumero()) != null) {
            System.out.println("Erreur : un animal avec le numéro " + animal.getNumero() + " existe déjà.");
            return false;
        }
        boolean ok = ((ZoneElevage) zone).ajouterAnimal(animal);
        if (ok) System.out.println("Animal #" + animal.getNumero() + " affecté à la zone " + codeZone + ".");
        return ok;
    }

    public void afficherVueEnsembleZones() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       VUE D'ENSEMBLE DES ZONES - " + nomFerme);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        int i = 1;
        for (Zone z : zones.values()) {
            System.out.println("║  " + i++ + ". " + z);
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    public void enregistrerProduction(String codeZone, double valeur, String date) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return;
        }
        zone.enregistrerProduction(valeur, date);
        System.out.println("Production enregistrée pour la zone " + codeZone + " : " + valeur + " le " + date);
    }

    public void afficherProductionParPlageDate(String codeZone, String dateDebut, String dateFin) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return;
        }
        HistoriqueProduction historique = zone.getHistoriqueProduction();
        System.out.println("\n--- Production filtrée - Zone " + codeZone + " ---");
        boolean found = false;
        for (int i = 0; i < historique.getNbEntrees(); i++) {
            String date = historique.getDate(i);
            if ((dateDebut == null || date.compareTo(dateDebut) >= 0)
                    && (dateFin == null || date.compareTo(dateFin) <= 0)) {
                System.out.println("  " + date + " : " + historique.getValeur(i)
                        + " " + historique.getUnite());
                found = true;
            }
        }
        if (!found) {
            System.out.println("  Aucune production dans cette plage de dates.");
        }
    }

    // ======================== IGestionCultures ========================

    public boolean enregistrerCulture(Culture culture) {
        for (Zone z : zones.values()) {
            if (z instanceof ZoneCulture) {
                return ((ZoneCulture) z).ajouterCulture(culture);
            }
        }
        System.out.println("Erreur : aucune zone de culture disponible.");
        return false;
    }

    public boolean mettreAJourStade(String codeZone, int indexCulture, StadeCroissance nouveauStade) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null || !(zone instanceof ZoneCulture)) {
            System.out.println("Erreur : zone de culture " + codeZone + " introuvable.");
            return false;
        }
        Culture culture = ((ZoneCulture) zone).getCulture(indexCulture);
        if (culture == null) {
            System.out.println("Erreur : culture à l'index " + indexCulture + " introuvable.");
            return false;
        }
        culture.setStadeActuel(nouveauStade);
        System.out.println("Culture \"" + culture.getNom() + "\" mise à jour : stade " + nouveauStade.getLibelle());
        return true;
    }

    public void afficherStadeCroissance(String codeZone) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null || !(zone instanceof ZoneCulture)) {
            System.out.println("Erreur : zone de culture " + codeZone + " introuvable.");
            return;
        }
        ZoneCulture zc = (ZoneCulture) zone;
        System.out.println("\n--- Stades de croissance - Zone " + codeZone + " ---");
        for (int i = 0; i < zc.getNbCultures(); i++) {
            Culture c = zc.getCulture(i);
            System.out.println("  " + c.getNom() + " : " + c.getStadeActuel().getLibelle());
        }
    }

    public void genererRapportCulturesParZone(String codeZone) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null || !(zone instanceof ZoneCulture)) {
            System.out.println("Erreur : zone de culture " + codeZone + " introuvable.");
            return;
        }
        ((ZoneCulture) zone).genererRapport();
    }

    public void afficherCulturesParFamille(String codeZone, FamilleCulture famille) {
        if (famille == null) {
            System.out.println("Erreur : famille de culture non spécifiée.");
            return;
        }

        System.out.println("\n--- Cultures filtrées par famille : " + famille.getLibelle() + " ---");
        boolean found = false;

        for (Zone z : zones.values()) {
            if (!(z instanceof ZoneCulture)) {
                continue;
            }
            if (codeZone != null && !z.getCode().equals(codeZone)) {
                continue;
            }

            ZoneCulture zc = (ZoneCulture) z;
            for (int i = 0; i < zc.getNbCultures(); i++) {
                Culture c = zc.getCulture(i);
                if (c.getFamille() == famille) {
                    System.out.println("  Zone " + zc.getCode() + " - " + c.getNom()
                            + " | Stade: " + c.getStadeActuel().getLibelle());
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("  Aucune culture trouvée pour cette famille.");
        }
    }

    // ======================== IGestionAnimaux ========================

    public boolean enregistrerAnimal(Animal animal) {
        if (rechercherAnimal(animal.getNumero()) != null) {
            System.out.println("Erreur : un animal avec le numéro " + animal.getNumero() + " existe déjà.");
            return false;
        }
        for (Zone z : zones.values()) {
            if (z instanceof ZoneElevage) {
                ZoneElevage ze = (ZoneElevage) z;
                if (ze.getTypeElevage() == animal.getTypeElevage()) {
                    return ze.ajouterAnimal(animal);
                }
            }
        }
        System.out.println("Erreur : aucune zone d'élevage compatible disponible.");
        return false;
    }

    public boolean mettreAJourSante(int numeroAnimal, EtatSante nouvelEtat) {
        Animal animal = rechercherAnimal(numeroAnimal);
        if (animal == null) {
            System.out.println("Erreur : animal #" + numeroAnimal + " introuvable.");
            return false;
        }
        animal.setEtatSante(nouvelEtat);
        System.out.println("Animal #" + numeroAnimal + " : état de santé mis à jour -> " + nouvelEtat.getLibelle());
        return true;
    }

    public boolean enregistrerEvenementSanitaire(int numeroAnimal, String description, String date) {
        Animal animal = rechercherAnimal(numeroAnimal);
        if (animal == null) {
            System.out.println("Erreur : animal #" + numeroAnimal + " introuvable.");
            return false;
        }
        return animal.ajouterEvenementSanitaire(date, description);
    }

    public boolean enregistrerEvolutionPoids(int numeroAnimal, double nouveauPoids, String date) {
        Animal animal = rechercherAnimal(numeroAnimal);
        if (animal == null) {
            System.out.println("Erreur : animal #" + numeroAnimal + " introuvable.");
            return false;
        }
        return animal.enregistrerPoids(date, nouveauPoids);
    }

    public void afficherAnimauxParEtatSante(String codeZone, EtatSante etat) {
        if (etat == null) {
            System.out.println("Erreur : état de santé non spécifié.");
            return;
        }

        System.out.println("\n--- Animaux filtrés par état : " + etat.getLibelle() + " ---");
        boolean auMoinsUneZone = false;

        for (Zone z : zones.values()) {
            if (!(z instanceof ZoneElevage)) {
                continue;
            }
            if (codeZone != null && !z.getCode().equals(codeZone)) {
                continue;
            }
            auMoinsUneZone = true;
            System.out.println(" Zone " + z.getCode() + " - " + z.getNom());
            ((ZoneElevage) z).afficherAnimauxParEtatSante(etat);
        }

        if (!auMoinsUneZone) {
            if (codeZone == null) {
                System.out.println("  Aucune zone d'élevage disponible.");
            } else {
                System.out.println("  Zone d'élevage " + codeZone + " introuvable.");
            }
        }
    }

    public void afficherProgrammeAlimentation(String codeZone) {
        Zone zone = rechercherZone(codeZone);
        if (zone == null) {
            System.out.println("Erreur : zone " + codeZone + " introuvable.");
            return;
        }
        System.out.println("\n--- Programme d'alimentation - Zone " + codeZone + " ---");
        if (zone instanceof ZoneElevage) {
            ProgrammeAlimentation prog = ((ZoneElevage) zone).getProgrammeAlimentation();
            if (prog != null) System.out.println("  " + prog);
            else System.out.println("  Aucun programme défini.");
        } else if (zone instanceof ZoneAquacole) {
            ProgrammeAlimentation prog = ((ZoneAquacole) zone).getProgrammeAlimentation();
            if (prog != null) System.out.println("  " + prog);
            else System.out.println("  Aucun programme défini.");
        } else {
            System.out.println("  Cette zone ne dispose pas de programme d'alimentation.");
        }
    }

    private Animal rechercherAnimal(int numero) {
        for (Zone z : zones.values()) {
            if (z instanceof ZoneElevage) {
                Animal a = ((ZoneElevage) z).rechercherAnimalParNumero(numero);
                if (a != null) return a;
            }
        }
        return null;
    }

    // ======================== IGestionCapteurs ========================

    public boolean ajouterCapteur(Capteur capteur) {
        if (rechercherCapteur(capteur.getCode()) != null) {
            System.out.println("Erreur : un capteur avec le code " + capteur.getCode() + " existe déjà.");
            return false;
        }
        capteurs.put(capteur.getCode(), capteur);
        Zone z = rechercherZone(capteur.getCodeZone());
        if (z != null) z.ajouterCapteurAssoc(capteur);
        System.out.println("Capteur ajouté : " + capteur);
        return true;
    }

    public boolean configurerCapteur(String codeCapteur, double seuilMin, double seuilMax) {
        Capteur capteur = rechercherCapteur(codeCapteur);
        if (capteur == null) {
            System.out.println("Erreur : capteur " + codeCapteur + " introuvable.");
            return false;
        }
        capteur.setSeuilMin(seuilMin);
        capteur.setSeuilMax(seuilMax);
        System.out.println("Capteur " + codeCapteur + " configuré : seuils [" + seuilMin + " - " + seuilMax + "]");
        return true;
    }

    public boolean changerStatutCapteur(String codeCapteur, StatutCapteur nouveauStatut) {
        Capteur capteur = rechercherCapteur(codeCapteur);
        if (capteur == null) {
            System.out.println("Erreur : capteur " + codeCapteur + " introuvable.");
            return false;
        }
        capteur.setStatut(nouveauStatut);
        System.out.println("Capteur " + codeCapteur + " : statut -> " + nouveauStatut.getLibelle());
        return true;
    }

    public int chargerCapteursDepuisFichier(String cheminFichier) {
        int nbLus = 0;
        int nbAcceptes = 0;
        int nbErreurs = 0;

        System.out.println("\n--- Chargement des capteurs depuis : " + cheminFichier + " ---");

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
                if (morceaux.length < 6) {
                    System.out.println("  Ligne " + numeroLigne + " ignorée (format invalide) : " + brute);
                    nbErreurs++;
                    continue;
                }

                String type = morceaux[0].trim().toUpperCase();
                String code = morceaux[1].trim();

                try {
                    Capteur capteur = construireCapteurDepuisLigne(type, code, morceaux);
                    if (capteur == null) {
                        System.out.println("  Ligne " + numeroLigne + " ignorée (type inconnu) : " + brute);
                        nbErreurs++;
                        continue;
                    }

                    if (ajouterCapteur(capteur)) {
                        nbAcceptes++;
                    } else {
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

        System.out.println("  Lignes de données : " + nbLus
                + " | Capteurs enregistrés : " + nbAcceptes
                + " | Erreurs : " + nbErreurs);
        return nbAcceptes;
    }

    public int chargerCapteursDepuisFichiers(String[] cheminsFichiers) {
        int total = 0;
        if (cheminsFichiers == null) {
            return total;
        }
        for (int i = 0; i < cheminsFichiers.length; i++) {
            if (cheminsFichiers[i] == null || cheminsFichiers[i].trim().isEmpty()) {
                continue;
            }
            total += chargerCapteursDepuisFichier(cheminsFichiers[i].trim());
        }
        return total;
    }

    private Capteur construireCapteurDepuisLigne(String type, String code, String[] morceaux) {
        if ("ENV".equals(type) || "ENVIRONNEMENTAL".equals(type)) {
            double seuilMin = Double.parseDouble(morceaux[3].trim());
            double seuilMax = Double.parseDouble(morceaux[4].trim());
            TypeCapteurEnv sousType = TypeCapteurEnv.valueOf(morceaux[5].trim().toUpperCase());
            return new CapteurEnvironnemental(code, morceaux[2].trim(), seuilMin, seuilMax, sousType);
        }
        if ("SOL".equals(type)) {
            double seuilMin = Double.parseDouble(morceaux[3].trim());
            double seuilMax = Double.parseDouble(morceaux[4].trim());
            TypeCapteurSol sousType = TypeCapteurSol.valueOf(morceaux[5].trim().toUpperCase());
            return new CapteurSol(code, morceaux[2].trim(), seuilMin, seuilMax, sousType);
        }
        if ("BIO".equals(type)) {
            double seuilMin = Double.parseDouble(morceaux[3].trim());
            double seuilMax = Double.parseDouble(morceaux[4].trim());
            TypeCapteurBio sousType = TypeCapteurBio.valueOf(morceaux[5].trim().toUpperCase());
            int numeroAnimal = Integer.parseInt(morceaux[6].trim());
            return new CapteurBiometrique(code, morceaux[2].trim(), seuilMin, seuilMax, sousType, numeroAnimal);
        }
        if ("GPS".equals(type)) {
            int numeroAnimal = Integer.parseInt(morceaux[3].trim());
            double latitude = Double.parseDouble(morceaux[4].trim());
            double longitude = Double.parseDouble(morceaux[5].trim());
            double rayon = Double.parseDouble(morceaux[6].trim());
            return new CapteurGPS(code, morceaux[2].trim(), numeroAnimal, latitude, longitude, rayon);
        }
        if ("EAU".equals(type)) {
            double seuilMin = Double.parseDouble(morceaux[3].trim());
            double seuilMax = Double.parseDouble(morceaux[4].trim());
            TypeCapteurEau sousType = TypeCapteurEau.valueOf(morceaux[5].trim().toUpperCase());
            return new CapteurEau(code, morceaux[2].trim(), seuilMin, seuilMax, sousType);
        }
        return null;
    }

    public int chargerRelevesDepuisFichier(String cheminFichier) {
        int nbLus = 0;
        int nbAcceptes = 0;
        int nbErreurs = 0;

        System.out.println("\n--- Chargement des relevés depuis : " + cheminFichier + " ---");

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
                if (morceaux.length < 5) {
                    System.out.println("  Ligne " + numeroLigne + " ignorée (format invalide) : " + brute);
                    nbErreurs++;
                    continue;
                }

                String type = morceaux[0].trim().toUpperCase();
                String codeCapteur = morceaux[1].trim();

                try {
                    boolean ok;
                    if ("NUM".equals(type)) {
                        double valeur = Double.parseDouble(morceaux[2].trim());
                        String unite = morceaux[3].trim();
                        String date = ValidationUtils.validerDateReleve(morceaux[4].trim(), "date de relevé");
                        ok = enregistrerReleve(codeCapteur, new ReleveNumerique(valeur, unite, date, codeCapteur));
                    } else if ("GPS".equals(type)) {
                        double latitude = Double.parseDouble(morceaux[2].trim());
                        double longitude = Double.parseDouble(morceaux[3].trim());
                        String date = ValidationUtils.validerDateReleve(morceaux[4].trim(), "date de relevé");
                        ok = enregistrerReleve(codeCapteur,
                                new ReleveGPS(new PositionGPS(latitude, longitude), date, codeCapteur));
                    } else {
                        System.out.println("  Ligne " + numeroLigne + " ignorée (type inconnu) : " + brute);
                        nbErreurs++;
                        continue;
                    }

                    if (ok) {
                        nbAcceptes++;
                    } else {
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

        System.out.println("  Lignes de données : " + nbLus
                + " | Relevés enregistrés : " + nbAcceptes
                + " | Erreurs : " + nbErreurs);
        return nbAcceptes;
    }

    public int chargerRelevesDepuisFichiers(String[] cheminsFichiers) {
        int total = 0;
        if (cheminsFichiers == null) {
            return total;
        }
        for (int i = 0; i < cheminsFichiers.length; i++) {
            if (cheminsFichiers[i] == null || cheminsFichiers[i].trim().isEmpty()) {
                continue;
            }
            total += chargerRelevesDepuisFichier(cheminsFichiers[i].trim());
        }
        return total;
    }

    public void afficherTableauDeBord(String codeZone) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  TABLEAU DE BORD DES CAPTEURS - Zone " + codeZone);
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        int compteur = 0;
        for (Capteur c : capteurs.values()) {
            if (c.getCodeZone().equals(codeZone)) {
                String indicateur = indicateurStatutCapteur(c.getStatut());
                System.out.println("║  " + indicateur + " " + c);

                if (c.getNbReleves() > 0) {
                    Releve dernier = c.getReleve(c.getNbReleves() - 1);
                    String resumeNiveau = indicateurNiveau(dernier.getNiveau());
                    System.out.println("║       Dernier relevé : "
                            + colorerParNiveau(dernier.toString(), dernier.getNiveau())
                            + " " + resumeNiveau);
                }
                compteur++;
            }
        }
        if (compteur == 0) System.out.println("║  Aucun capteur dans cette zone.");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    public void consulterHistoriqueReleves(String codeCapteur, String dateDebut, String dateFin) {
        Capteur capteur = rechercherCapteur(codeCapteur);
        if (capteur == null) {
            System.out.println("Erreur : capteur " + codeCapteur + " introuvable.");
            return;
        }
        capteur.afficherHistorique(dateDebut, dateFin);
    }

    public boolean enregistrerReleve(String codeCapteur, Releve releve) {
        Capteur capteur = rechercherCapteur(codeCapteur);
        if (capteur == null) {
            System.out.println("Erreur : capteur " + codeCapteur + " introuvable.");
            return false;
        }
        Zone zone = rechercherZone(capteur.getCodeZone());
        if (zone == null) {
            System.out.println("Erreur : zone " + capteur.getCodeZone()
                    + " introuvable pour le capteur " + codeCapteur + ".");
            return false;
        }
        if (zone.getStatut() != StatutZone.ACTIVE) {
            System.out.println("  Zone " + zone.getCode() + " suspendue, relevé ignoré pour le capteur "
                    + codeCapteur + ".");
            return false;
        }
        boolean ok = capteur.enregistrerReleve(releve);
        if (ok) {
            synchroniserPositionAnimalSiGPS(capteur, releve);
        }
        if (ok && releve.getNiveau() != NiveauGravite.NORMAL) {
            genererAlerte(releve, capteur);
        }
        return ok;
    }

    private Capteur rechercherCapteur(String codeCapteur) {
        return capteurs.get(codeCapteur);
    }

    private void synchroniserPositionAnimalSiGPS(Capteur capteur, Releve releve) {
        if (!(capteur instanceof CapteurGPS) || !releve.estGPS() || releve.getPosition() == null) {
            return;
        }
        CapteurGPS capteurGPS = (CapteurGPS) capteur;
        Animal animal = rechercherAnimal(capteurGPS.getNumeroAnimal());
        if (animal == null) {
            System.out.println("Attention : animal #" + capteurGPS.getNumeroAnimal()
                    + " introuvable pour le relevé GPS " + capteur.getCode() + ".");
            return;
        }
        animal.setPositionActuelle(releve.getPosition());
    }

    private String indicateurStatutCapteur(StatutCapteur statut) {
        switch (statut) {
            case ACTIF:
                return ANSI_VERT + "[OK]" + ANSI_RESET;
            case DEFAILLANT:
                return ANSI_ROUGE + "[!!]" + ANSI_RESET;
            default:
                return ANSI_JAUNE + "[--]" + ANSI_RESET;
        }
    }

    private String indicateurNiveau(NiveauGravite niveau) {
        switch (niveau) {
            case CRITIQUE:
                return ANSI_ROUGE + "*** CRITIQUE ***" + ANSI_RESET;
            case AVERTISSEMENT:
                return ANSI_JAUNE + "* AVERTISSEMENT *" + ANSI_RESET;
            default:
                return ANSI_VERT + "* NORMAL *" + ANSI_RESET;
        }
    }

    private String colorerParNiveau(String texte, NiveauGravite niveau) {
        switch (niveau) {
            case CRITIQUE:
                return ANSI_ROUGE + texte + ANSI_RESET;
            case AVERTISSEMENT:
                return ANSI_JAUNE + texte + ANSI_RESET;
            default:
                return ANSI_VERT + texte + ANSI_RESET;
        }
    }

    private void genererAlerte(Releve releve, Capteur capteur) {
        if (alertes.size() >= MAX_ALERTES) {
            System.out.println("Attention : nombre maximal d'alertes atteint.");
            return;
        }
        Alerte alerte = new Alerte(releve, releve.getNiveau(), capteur.getCodeZone());
        alertes.add(alerte);
        System.out.println("  >> ALERTE GÉNÉRÉE : " + alerte);
    }

    // ======================== IGestionAlertes ========================

    public void afficherPanneauAlertes() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              PANNEAU D'ALERTES - " + nomFerme);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        boolean aAlerte = false;
        for (int i = 0; i < alertes.size(); i++) {
            System.out.println("║  " + alertes.get(i));
            aAlerte = true;
        }
        if (!aAlerte) System.out.println("║  Aucune alerte enregistrée.");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    public void afficherAlertesActives() {
        System.out.println("\n--- Alertes actives uniquement ---");
        boolean found = false;
        for (int i = 0; i < alertes.size(); i++) {
            if (!alertes.get(i).estAcquittee()) {
                System.out.println("  " + alertes.get(i));
                found = true;
            }
        }
        if (!found) System.out.println("  Aucune alerte active.");
    }

    public boolean acquitterAlerte(int idAlerte) {
        for (int i = 0; i < alertes.size(); i++) {
            if (alertes.get(i).getId() == idAlerte) {
                alertes.get(i).acquitter();
                System.out.println("Alerte #" + idAlerte + " acquittée.");
                return true;
            }
        }
        System.out.println("Erreur : alerte #" + idAlerte + " introuvable.");
        return false;
    }

    public boolean supprimerAlerte(int idAlerte) {
        for (int i = 0; i < alertes.size(); i++) {
            if (alertes.get(i).getId() == idAlerte) {
                alertes.remove(i);
                System.out.println("Alerte #" + idAlerte + " supprimée.");
                return true;
            }
        }
        System.out.println("Erreur : alerte #" + idAlerte + " introuvable.");
        return false;
    }

    public void consulterHistoriqueAlertes(String codeZone, NiveauGravite niveau,
                                            String typeCapteur, String dateDebut, String dateFin) {
        System.out.println("\n--- Historique des alertes (filtré) ---");
        boolean found = false;
        for (int i = 0; i < alertes.size(); i++) {
            boolean match = true;
            if (codeZone != null && !alertes.get(i).getCodeZone().equals(codeZone)) match = false;
            if (niveau != null && alertes.get(i).getNiveau() != niveau) match = false;
            if (dateDebut != null && alertes.get(i).getDateCreation().compareTo(dateDebut) < 0) match = false;
            if (dateFin != null && alertes.get(i).getDateCreation().compareTo(dateFin) > 0) match = false;
            if (typeCapteur != null) {
                Capteur c = rechercherCapteur(alertes.get(i).getReleve().getCodeCapteur());
                if (c == null || !c.getTypeCapteur().contains(typeCapteur)) match = false;
            }
            if (match) {
                System.out.println("  " + alertes.get(i));
                found = true;
            }
        }
        if (!found) System.out.println("  Aucune alerte correspondant aux critères.");
    }

    public void trierAlertesParGravite() {
        for (int i = 0; i < alertes.size() - 1; i++) {
            int indexMax = i;
            for (int j = i + 1; j < alertes.size(); j++) {
                if (alertes.get(j).getNiveau().ordinal() > alertes.get(indexMax).getNiveau().ordinal()) {
                    indexMax = j;
                }
            }
            if (indexMax != i) {
                Alerte temp = alertes.get(i);
                alertes.set(i, alertes.get(indexMax));
                alertes.set(indexMax, temp);
            }
        }
        System.out.println("Alertes triées par niveau de gravité.");
    }

    public void afficherAlertesTrierParGravite() {
        trierAlertesParGravite();
        afficherPanneauAlertes();
    }
}
