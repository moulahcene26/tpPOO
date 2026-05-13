package ferme.models;

import java.util.ArrayList;
import java.util.List;

public class HistoriqueProduction {

    private List<String> dates;
    private List<Double> valeurs;
    private String unite;

    public HistoriqueProduction(String unite) {
        this.dates = new ArrayList<>();
        this.valeurs = new ArrayList<>();
        this.unite = unite;
    }

    public boolean ajouterEntree(String date, double valeur) {
        dates.add(date);
        valeurs.add(valeur);
        return true;
    }

    public int getNbEntrees() { return dates.size(); }
    public String getUnite() { return unite; }

    public String getDate(int index) {
        if (index >= 0 && index < dates.size()) return dates.get(index);
        return null;
    }

    public double getValeur(int index) {
        if (index >= 0 && index < valeurs.size()) return valeurs.get(index);
        return -1;
    }

    public double moyenneProduction() {
        if (dates.size() == 0) return 0;
        double somme = 0;
        for (int i = 0; i < valeurs.size(); i++) {
            somme += valeurs.get(i);
        }
        return somme / valeurs.size();
    }

    public void afficher() {
        System.out.println("--- Historique de production (" + unite + ") ---");
        for (int i = 0; i < dates.size(); i++) {
            System.out.println("  " + dates.get(i) + " : " + valeurs.get(i) + " " + unite);
        }
        if (dates.size() > 0) {
            System.out.println("  Moyenne : " + String.format("%.2f", moyenneProduction()) + " " + unite);
        }
    }
}
