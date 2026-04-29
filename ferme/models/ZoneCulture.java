package ferme.models;

public class ZoneCulture extends Zone {
    public static final int MAX_CULTURES = 50;

    private Culture[] cultures;
    private int nbCultures;

    public ZoneCulture(String code, String nom) {
        super(code, nom, "kg");
        this.cultures = new Culture[MAX_CULTURES];
        this.nbCultures = 0;
    }

    public boolean ajouterCulture(Culture culture) {
        if (nbCultures >= MAX_CULTURES) return false;
        cultures[nbCultures] = culture;
        nbCultures++;
        return true;
    }

    public Culture getCulture(int index) {
        if (index >= 0 && index < nbCultures) return cultures[index];
        return null;
    }

    public int getNbCultures() { return nbCultures; }

    public String getTypeZone() { return "Culture"; }

    public int getNombreEntites() { return nbCultures; }

    public void afficherDetails() {
        System.out.println("=== Zone de Culture : " + nom + " (" + code + ") ===");
        System.out.println("Statut : " + statut.getLibelle());
        System.out.println("Nombre de cultures : " + nbCultures);
        for (int i = 0; i < nbCultures; i++) {
            System.out.println("  " + (i + 1) + ". " + cultures[i]);
        }
        historiqueProduction.afficher();
    }

    public void genererRapport() {
        System.out.println("\n--- Rapport des cultures - Zone " + nom + " (" + code + ") ---");
        int[] compteurParFamille = new int[ferme.enums.FamilleCulture.values().length];
        int[] compteurParStade = new int[ferme.enums.StadeCroissance.values().length];

        for (int i = 0; i < nbCultures; i++) {
            compteurParFamille[cultures[i].getFamille().ordinal()]++;
            compteurParStade[cultures[i].getStadeActuel().ordinal()]++;
        }

        System.out.println("  Par famille :");
        for (ferme.enums.FamilleCulture f : ferme.enums.FamilleCulture.values()) {
            if (compteurParFamille[f.ordinal()] > 0) {
                System.out.println("    " + f.getLibelle() + " : " + compteurParFamille[f.ordinal()]);
            }
        }
        System.out.println("  Par stade de croissance :");
        for (ferme.enums.StadeCroissance s : ferme.enums.StadeCroissance.values()) {
            if (compteurParStade[s.ordinal()] > 0) {
                System.out.println("    " + s.getLibelle() + " : " + compteurParStade[s.ordinal()]);
            }
        }
    }
}
