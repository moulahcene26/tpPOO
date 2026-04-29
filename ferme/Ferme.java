package ferme;

import ferme.enums.*;
import ferme.interfaces.*;
import ferme.models.*;

public class Ferme implements IGestionZones, IGestionCultures, IGestionAnimaux, IGestionCapteurs, IGestionAlertes {

    public static final int MAX_ZONES = 50;
    public static final int MAX_CAPTEURS = 200;
    public static final int MAX_ALERTES = 500;

    private String nomFerme;
    private Zone[] zones;
    private int nbZones;
    private Capteur[] capteurs;
    private int nbCapteurs;
    private Alerte[] alertes;
    private int nbAlertes;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_VERT = "\u001B[32m";
    private static final String ANSI_JAUNE = "\u001B[33m";
    private static final String ANSI_ROUGE = "\u001B[31m";

    public Ferme(String nomFerme) {
        this.nomFerme = nomFerme;
        this.zones = new Zone[MAX_ZONES];
        this.nbZones = 0;
        this.capteurs = new Capteur[MAX_CAPTEURS];
        this.nbCapteurs = 0;
        this.alertes = new Alerte[MAX_ALERTES];
        this.nbAlertes = 0;
    }

    public String getNomFerme() { return nomFerme; }

    // ======================== IGestionZones ========================

    public boolean ajouterZone(Zone zone) {
        if (nbZones >= MAX_ZONES) {
            System.out.println("Erreur : nombre maximal de zones atteint.");
            return false;
        }
        if (rechercherZone(zone.getCode()) != null) {
            System.out.println("Erreur : une zone avec le code " + zone.getCode() + " existe déjà.");
            return false;
        }
        zones[nbZones] = zone;
        nbZones++;
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
        // Réactiver tous les capteurs de cette zone
        for (int i = 0; i < nbCapteurs; i++) {
            if (capteurs[i].getCodeZone().equals(codeZone)) {
                capteurs[i].reactiver();
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
        // Suspendre tous les capteurs de cette zone
        for (int i = 0; i < nbCapteurs; i++) {
            if (capteurs[i].getCodeZone().equals(codeZone)) {
                capteurs[i].suspendre();
            }
        }
        System.out.println("Zone " + codeZone + " suspendue. Capteurs associés suspendus.");
        return true;
    }

    public Zone rechercherZone(String codeZone) {
        for (int i = 0; i < nbZones; i++) {
            if (zones[i].getCode().equals(codeZone)) return zones[i];
        }
        return null;
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
        for (int i = 0; i < nbZones; i++) {
            System.out.println("║  " + (i + 1) + ". " + zones[i]);
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

    // ======================== IGestionCultures ========================

    public boolean enregistrerCulture(Culture culture) {
        // Trouve la première zone de culture disponible
        for (int i = 0; i < nbZones; i++) {
            if (zones[i] instanceof ZoneCulture) {
                return ((ZoneCulture) zones[i]).ajouterCulture(culture);
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

    // ======================== IGestionAnimaux ========================

    public boolean enregistrerAnimal(Animal animal) {
        if (rechercherAnimal(animal.getNumero()) != null) {
            System.out.println("Erreur : un animal avec le numéro " + animal.getNumero() + " existe déjà.");
            return false;
        }
        for (int i = 0; i < nbZones; i++) {
            if (zones[i] instanceof ZoneElevage) {
                ZoneElevage ze = (ZoneElevage) zones[i];
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
        for (int i = 0; i < nbZones; i++) {
            if (zones[i] instanceof ZoneElevage) {
                Animal a = ((ZoneElevage) zones[i]).rechercherAnimalParNumero(numero);
                if (a != null) return a;
            }
        }
        return null;
    }

    // ======================== IGestionCapteurs ========================

    public boolean ajouterCapteur(Capteur capteur) {
        if (nbCapteurs >= MAX_CAPTEURS) {
            System.out.println("Erreur : nombre maximal de capteurs atteint.");
            return false;
        }
        if (rechercherCapteur(capteur.getCode()) != null) {
            System.out.println("Erreur : un capteur avec le code " + capteur.getCode() + " existe déjà.");
            return false;
        }
        capteurs[nbCapteurs] = capteur;
        nbCapteurs++;
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

    public void afficherTableauDeBord(String codeZone) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  TABLEAU DE BORD DES CAPTEURS - Zone " + codeZone);
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        int compteur = 0;
        for (int i = 0; i < nbCapteurs; i++) {
            if (capteurs[i].getCodeZone().equals(codeZone)) {
                String indicateur = indicateurStatutCapteur(capteurs[i].getStatut());
                System.out.println("║  " + indicateur + " " + capteurs[i]);

                // Dernier relevé
                if (capteurs[i].getNbReleves() > 0) {
                    Releve dernier = capteurs[i].getReleve(capteurs[i].getNbReleves() - 1);
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

    public void afficherGraphiqueEvolution(String codeCapteur) {
        Capteur capteur = rechercherCapteur(codeCapteur);
        if (capteur == null) {
            System.out.println("Erreur : capteur " + codeCapteur + " introuvable.");
            return;
        }
        capteur.afficherGraphique();
    }

    public void afficherGraphiqueParZone(String codeZone) {
        System.out.println("\n=== GRAPHIQUES D'ÉVOLUTION - Zone " + codeZone + " ===");
        for (int i = 0; i < nbCapteurs; i++) {
            if (capteurs[i].getCodeZone().equals(codeZone)) {
                capteurs[i].afficherGraphique();
            }
        }
    }

    private Capteur rechercherCapteur(String codeCapteur) {
        for (int i = 0; i < nbCapteurs; i++) {
            if (capteurs[i].getCode().equals(codeCapteur)) return capteurs[i];
        }
        return null;
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
        if (nbAlertes >= MAX_ALERTES) {
            System.out.println("Attention : nombre maximal d'alertes atteint.");
            return;
        }
        Alerte alerte = new Alerte(releve, releve.getNiveau(), capteur.getCodeZone());
        alertes[nbAlertes] = alerte;
        nbAlertes++;
        System.out.println("  >> ALERTE GÉNÉRÉE : " + alerte);
    }

    // ======================== IGestionAlertes ========================

    public void afficherPanneauAlertes() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              PANNEAU D'ALERTES - " + nomFerme);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        // Critiques d'abord
        boolean aAlerte = false;
        for (int i = 0; i < nbAlertes; i++) {
            if (!alertes[i].estAcquittee() && alertes[i].getNiveau() == NiveauGravite.CRITIQUE) {
                System.out.println("║  " + alertes[i]);
                aAlerte = true;
            }
        }
        for (int i = 0; i < nbAlertes; i++) {
            if (!alertes[i].estAcquittee() && alertes[i].getNiveau() == NiveauGravite.AVERTISSEMENT) {
                System.out.println("║  " + alertes[i]);
                aAlerte = true;
            }
        }
        if (!aAlerte) System.out.println("║  Aucune alerte active.");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    public void afficherAlertesActives() {
        System.out.println("\n--- Alertes actives ---");
        boolean found = false;
        for (int i = 0; i < nbAlertes; i++) {
            if (!alertes[i].estAcquittee()) {
                System.out.println("  " + alertes[i]);
                found = true;
            }
        }
        if (!found) System.out.println("  Aucune alerte active.");
    }

    public boolean acquitterAlerte(int idAlerte) {
        for (int i = 0; i < nbAlertes; i++) {
            if (alertes[i].getId() == idAlerte) {
                alertes[i].acquitter();
                System.out.println("Alerte #" + idAlerte + " acquittée.");
                return true;
            }
        }
        System.out.println("Erreur : alerte #" + idAlerte + " introuvable.");
        return false;
    }

    public boolean supprimerAlerte(int idAlerte) {
        for (int i = 0; i < nbAlertes; i++) {
            if (alertes[i].getId() == idAlerte) {
                for (int j = i; j < nbAlertes - 1; j++) {
                    alertes[j] = alertes[j + 1];
                }
                alertes[nbAlertes - 1] = null;
                nbAlertes--;
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
        for (int i = 0; i < nbAlertes; i++) {
            boolean match = true;
            if (codeZone != null && !alertes[i].getCodeZone().equals(codeZone)) match = false;
            if (niveau != null && alertes[i].getNiveau() != niveau) match = false;
            if (dateDebut != null && alertes[i].getDateCreation().compareTo(dateDebut) < 0) match = false;
            if (dateFin != null && alertes[i].getDateCreation().compareTo(dateFin) > 0) match = false;
            if (typeCapteur != null) {
                Capteur c = rechercherCapteur(alertes[i].getReleve().getCodeCapteur());
                if (c == null || !c.getTypeCapteur().contains(typeCapteur)) match = false;
            }
            if (match) {
                System.out.println("  " + alertes[i]);
                found = true;
            }
        }
        if (!found) System.out.println("  Aucune alerte correspondant aux critères.");
    }

    public void trierAlertesParGravite() {
        // Tri par sélection : critiques en premier
        for (int i = 0; i < nbAlertes - 1; i++) {
            int indexMax = i;
            for (int j = i + 1; j < nbAlertes; j++) {
                if (alertes[j].getNiveau().ordinal() > alertes[indexMax].getNiveau().ordinal()) {
                    indexMax = j;
                }
            }
            if (indexMax != i) {
                Alerte temp = alertes[i];
                alertes[i] = alertes[indexMax];
                alertes[indexMax] = temp;
            }
        }
        System.out.println("Alertes triées par niveau de gravité (critique en premier).");
    }
}
