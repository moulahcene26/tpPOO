package ferme.models;

import ferme.enums.StatutCapteur;
import ferme.enums.NiveauGravite;
import java.util.ArrayList;
import java.util.List;

public abstract class Capteur {

    protected String code;
    protected String codeZone;
    protected StatutCapteur statut;
    protected double seuilMin;
    protected double seuilMax;
    protected List<Releve> releves;

    public Capteur(String code, String codeZone, double seuilMin, double seuilMax) {
        this.code = code;
        this.codeZone = codeZone;
        this.statut = StatutCapteur.ACTIF;
        this.seuilMin = seuilMin;
        this.seuilMax = seuilMax;
        this.releves = new ArrayList<>();
    }

    public String getCode() { return code; }
    public String getCodeZone() { return codeZone; }
    public StatutCapteur getStatut() { return statut; }
    public double getSeuilMin() { return seuilMin; }
    public double getSeuilMax() { return seuilMax; }
    public int getNbReleves() { return releves.size(); }

    public void setStatut(StatutCapteur statut) { this.statut = statut; }
    public void setSeuilMin(double seuilMin) { this.seuilMin = seuilMin; }
    public void setSeuilMax(double seuilMax) { this.seuilMax = seuilMax; }
    public void setCodeZone(String codeZone) { this.codeZone = codeZone; }

    public void suspendre() { this.statut = StatutCapteur.SUSPENDU; }
    public void reactiver() { this.statut = StatutCapteur.ACTIF; }

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

        if (releve.estGPS()) {
            System.out.println("  Capteur " + code + " non GPS, relevé GPS ignoré.");
            return false;
        }

        NiveauGravite niveau = evaluerNiveau(releve.getValeur());
        releve.setNiveau(niveau);

        releves.add(releve);
        return true;
    }

    public Releve getReleve(int index) {
        if (index >= 0 && index < releves.size())
            return releves.get(index);
        return null;
    }

    public abstract String getTypeCapteur();
    public abstract String getUnite();

    public void afficherHistorique(String dateDebut, String dateFin) {
        System.out.println("  Historique du capteur " + code + " (" + getTypeCapteur() + ") :");
        for (int i = 0; i < releves.size(); i++) {
            if ((dateDebut == null || releves.get(i).getDate().compareTo(dateDebut) >= 0)
                    && (dateFin == null || releves.get(i).getDate().compareTo(dateFin) <= 0)) {
                System.out.println("    " + releves.get(i));
            }
        }
    }

    public void afficherGraphique() {
        System.out.println("\n  === Graphique d'évolution : Capteur " + code + " (" + getTypeCapteur() + ") ===");
        if (releves.size() == 0) {
            System.out.println("    Aucun relevé enregistré.");
            return;
        }

        int premierNumerique = -1;
        for (int i = 0; i < releves.size(); i++) {
            if (!releves.get(i).estGPS()) {
                premierNumerique = i;
                break;
            }
        }
        if (premierNumerique == -1) {
            System.out.println("    Aucun relevé numérique à afficher.");
            return;
        }

        double max = releves.get(premierNumerique).getValeur();
        double min = releves.get(premierNumerique).getValeur();
        for (int i = premierNumerique + 1; i < releves.size(); i++) {
            if (releves.get(i).estGPS()) continue;
            if (releves.get(i).getValeur() > max) max = releves.get(i).getValeur();
            if (releves.get(i).getValeur() < min) min = releves.get(i).getValeur();
        }

        System.out.println("    Seuils : [" + seuilMin + " - " + seuilMax + "]");
        System.out.println("    Plage relevée : [" + min + " - " + max + "]");
        int largeur = 40;
        double plage = max - min;
        if (plage == 0) plage = 1;

        for (int i = 0; i < releves.size(); i++) {
            if (releves.get(i).estGPS()) continue;
            double val = releves.get(i).getValeur();
            int pos = (int) ((val - min) / plage * largeur);
            if (pos < 0) pos = 0;
            if (pos > largeur) pos = largeur;

            String indicateur;
            switch (releves.get(i).getNiveau()) {
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
            System.out.println("    " + releves.get(i).getDate() + " " + indicateur
                    + "|" + barre + "| " + val + " " + getUnite());
        }
    }

    public String toString() {
        return "Capteur " + code + " [" + getTypeCapteur() + "] | Zone: " + codeZone
                + " | Statut: " + statut.getLibelle()
                + " | Seuils: [" + seuilMin + " - " + seuilMax + "]"
                + " | Relevés: " + releves.size();
    }
}
