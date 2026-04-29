package ferme.models;

import ferme.enums.StatutCapteur;
import ferme.enums.NiveauGravite;

public abstract class Capteur {
    public static final int MAX_RELEVES = 200;

    protected String code;
    protected String codeZone;
    protected StatutCapteur statut;
    protected double seuilMin;
    protected double seuilMax;
    protected Releve[] releves;
    protected int nbReleves;

    public Capteur(String code, String codeZone, double seuilMin, double seuilMax) {
        this.code = code;
        this.codeZone = codeZone;
        this.statut = StatutCapteur.ACTIF;
        this.seuilMin = seuilMin;
        this.seuilMax = seuilMax;
        this.releves = new Releve[MAX_RELEVES];
        this.nbReleves = 0;
    }

    public String getCode() {
        return code;
    }

    public String getCodeZone() {
        return codeZone;
    }

    public StatutCapteur getStatut() {
        return statut;
    }

    public double getSeuilMin() {
        return seuilMin;
    }

    public double getSeuilMax() {
        return seuilMax;
    }

    public int getNbReleves() {
        return nbReleves;
    }

    public void setStatut(StatutCapteur statut) {
        this.statut = statut;
    }

    public void setSeuilMin(double seuilMin) {
        this.seuilMin = seuilMin;
    }

    public void setSeuilMax(double seuilMax) {
        this.seuilMax = seuilMax;
    }

    public void setCodeZone(String codeZone) {
        this.codeZone = codeZone;
    }

    public void suspendre() {
        this.statut = StatutCapteur.SUSPENDU;
    }

    public void reactiver() {
        this.statut = StatutCapteur.ACTIF;
    }

    public NiveauGravite evaluerNiveau(double valeur) {
        if (valeur < seuilMin || valeur > seuilMax) {
            double ecart = 0;
            if (valeur < seuilMin)
                ecart = seuilMin - valeur;
            else
                ecart = valeur - seuilMax;
            double plage = seuilMax - seuilMin;
            if (plage > 0 && ecart / plage > 0.5) {
                return NiveauGravite.CRITIQUE;
            }
            return NiveauGravite.AVERTISSEMENT;
        }
        return NiveauGravite.NORMAL;
    }

    public boolean enregistrerReleve(Releve releve) {
        if (statut == StatutCapteur.SUSPENDU) {
            System.out.println("  Capteur " + code + " suspendu, relevé ignoré.");
            return false;
        }
        if (nbReleves >= MAX_RELEVES)
            return false;

        if (releve.estGPS()) {
            System.out.println("  Capteur " + code + " non GPS, relevé GPS ignoré.");
            return false;
        }

        NiveauGravite niveau = evaluerNiveau(releve.getValeur());
        releve.setNiveau(niveau);

        releves[nbReleves] = releve;
        nbReleves++;
        return true;
    }

    public Releve getReleve(int index) {
        if (index >= 0 && index < nbReleves)
            return releves[index];
        return null;
    }

    public abstract String getTypeCapteur();

    public abstract String getUnite();

    public void afficherHistorique(String dateDebut, String dateFin) {
        System.out.println("  Historique du capteur " + code + " (" + getTypeCapteur() + ") :");
        for (int i = 0; i < nbReleves; i++) {
            if ((dateDebut == null || releves[i].getDate().compareTo(dateDebut) >= 0)
                    && (dateFin == null || releves[i].getDate().compareTo(dateFin) <= 0)) {
                System.out.println("    " + releves[i]);
            }
        }
    }

    public void afficherGraphique() {
        System.out.println("\n  === Graphique d'évolution : Capteur " + code + " (" + getTypeCapteur() + ") ===");
        if (nbReleves == 0) {
            System.out.println("    Aucun relevé enregistré.");
            return;
        }

        int premierNumerique = -1;
        for (int i = 0; i < nbReleves; i++) {
            if (!releves[i].estGPS()) {
                premierNumerique = i;
                break;
            }
        }
        if (premierNumerique == -1) {
            System.out.println("    Aucun relevé numérique à afficher.");
            return;
        }

        double max = releves[premierNumerique].getValeur();
        double min = releves[premierNumerique].getValeur();
        for (int i = premierNumerique + 1; i < nbReleves; i++) {
            if (releves[i].estGPS())
                continue;
            if (releves[i].getValeur() > max)
                max = releves[i].getValeur();
            if (releves[i].getValeur() < min)
                min = releves[i].getValeur();
        }

        System.out.println("    Seuils : [" + seuilMin + " - " + seuilMax + "]");
        System.out.println("    Plage relevée : [" + min + " - " + max + "]");
        int largeur = 40;
        double plage = max - min;
        if (plage == 0)
            plage = 1;

        for (int i = 0; i < nbReleves; i++) {
            if (releves[i].estGPS())
                continue;
            double val = releves[i].getValeur();
            int pos = (int) ((val - min) / plage * largeur);
            if (pos < 0)
                pos = 0;
            if (pos > largeur)
                pos = largeur;

            String indicateur;
            switch (releves[i].getNiveau()) {
                case CRITIQUE:
                    indicateur = "!!! ";
                    break;
                case AVERTISSEMENT:
                    indicateur = " !  ";
                    break;
                default:
                    indicateur = " .  ";
                    break;
            }

            StringBuilder barre = new StringBuilder();
            for (int j = 0; j < largeur; j++) {
                barre.append(j == pos ? "#" : "-");
            }
            System.out.println("    " + releves[i].getDate() + " " + indicateur
                    + "|" + barre + "| " + val + " " + getUnite());
        }
    }

    public String toString() {
        return "Capteur " + code + " [" + getTypeCapteur() + "] | Zone: " + codeZone
                + " | Statut: " + statut.getLibelle()
                + " | Seuils: [" + seuilMin + " - " + seuilMax + "]"
                + " | Relevés: " + nbReleves;
    }
}
