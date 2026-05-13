package ferme.models;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ferme.enums.FamilleCulture;
import ferme.enums.StadeCroissance;

public class ZoneCulture extends Zone {

    private List<Culture> cultures;

    public ZoneCulture(String code, String nom) {
        super(code, nom, "kg");
        this.cultures = new ArrayList<>();
    }

    public boolean ajouterCulture(Culture culture) {
        cultures.add(culture);
        return true;
    }

    public Culture getCulture(int index) {
        if (index >= 0 && index < cultures.size()) return cultures.get(index);
        return null;
    }

    public int getNbCultures() { return cultures.size(); }

    public String getTypeZone() { return "Culture"; }

    public int getNombreEntites() { return cultures.size(); }

    public void afficherDetails() {
        System.out.println(" Zone de Culture : " + nom + " (" + code + ") ");
        System.out.println("Statut : " + statut.getLibelle());
        System.out.println("Nombre de cultures : " + cultures.size());
        for (int i = 0; i < cultures.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + cultures.get(i).getNom());
        }
        historiqueProduction.afficher();
    }

    public void genererRapport() {
        System.out.println("\nRapport des cultures - Zone " + nom + " (" + code + ")");

        Map<FamilleCulture, Integer> compteurParFamille = new LinkedHashMap<>();
        Map<StadeCroissance, Integer> compteurParStade  = new LinkedHashMap<>();

        for (FamilleCulture f : FamilleCulture.values()) compteurParFamille.put(f, 0);
        for (StadeCroissance s : StadeCroissance.values()) compteurParStade.put(s, 0);

        for (int i = 0; i < cultures.size(); i++) {
            FamilleCulture f = cultures.get(i).getFamille();
            StadeCroissance s = cultures.get(i).getStadeActuel();
            compteurParFamille.put(f, compteurParFamille.get(f) + 1);
            compteurParStade.put(s, compteurParStade.get(s) + 1);
        }

        System.out.println("  Par famille :");
        for (Map.Entry<FamilleCulture, Integer> entry : compteurParFamille.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println("    " + entry.getKey().getLibelle() + " : " + entry.getValue());
        }

        System.out.println("  Par stade de croissance :");
        for (Map.Entry<StadeCroissance, Integer> entry : compteurParStade.entrySet()) {
            if (entry.getValue() > 0)
                System.out.println("    " + entry.getKey().getLibelle() + " : " + entry.getValue());
        }
    }
}
