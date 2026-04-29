package ferme.models;

public class HistoriqueProduction {
    public static final int MAX_ENTREES = 100;

    private String[] dates;
    private double[] valeurs;
    private String unite;
    private int nbEntrees;

    public HistoriqueProduction(String unite) {
        this.dates = new String[MAX_ENTREES];
        this.valeurs = new double[MAX_ENTREES];
        this.unite = unite;
        this.nbEntrees = 0;
    }

    public boolean ajouterEntree(String date, double valeur) {
        if (nbEntrees >= MAX_ENTREES) return false;
        dates[nbEntrees] = date;
        valeurs[nbEntrees] = valeur;
        nbEntrees++;
        return true;
    }

    public int getNbEntrees() { return nbEntrees; }
    public String getUnite() { return unite; }

    public String getDate(int index) {
        if (index >= 0 && index < nbEntrees) return dates[index];
        return null;
    }

    public double getValeur(int index) {
        if (index >= 0 && index < nbEntrees) return valeurs[index];
        return -1;
    }

    public double moyenneProduction() {
        if (nbEntrees == 0) return 0;
        double somme = 0;
        for (int i = 0; i < nbEntrees; i++) {
            somme += valeurs[i];
        }
        return somme / nbEntrees;
    }

    public void afficher() {
        System.out.println("--- Historique de production (" + unite + ") ---");
        for (int i = 0; i < nbEntrees; i++) {
            System.out.println("  " + dates[i] + " : " + valeurs[i] + " " + unite);
        }
        if (nbEntrees > 0) {
            System.out.println("  Moyenne : " + String.format("%.2f", moyenneProduction()) + " " + unite);
        }
    }
}
