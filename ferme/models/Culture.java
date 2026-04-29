package ferme.models;

import ferme.enums.FamilleCulture;
import ferme.enums.StadeCroissance;

public class Culture {
    private String nom;
    private FamilleCulture famille;
    private String datePlantation;
    private String dateRecoltePrevue;
    private StadeCroissance stadeActuel;
    private ExigencesPedologiques exigences;

    public Culture(String nom, FamilleCulture famille, String datePlantation,
                   String dateRecoltePrevue, ExigencesPedologiques exigences) {
        this.nom = nom;
        this.famille = famille;
        this.datePlantation = datePlantation;
        this.dateRecoltePrevue = dateRecoltePrevue;
        this.stadeActuel = StadeCroissance.SEMIS;
        this.exigences = exigences;
    }

    public String getNom() { return nom; }
    public FamilleCulture getFamille() { return famille; }
    public String getDatePlantation() { return datePlantation; }
    public String getDateRecoltePrevue() { return dateRecoltePrevue; }
    public StadeCroissance getStadeActuel() { return stadeActuel; }
    public ExigencesPedologiques getExigences() { return exigences; }

    public void setStadeActuel(StadeCroissance stade) { this.stadeActuel = stade; }
    public void setDateRecoltePrevue(String date) { this.dateRecoltePrevue = date; }

    public String toString() {
        return nom + " [" + famille.getLibelle() + "] - Stade: " + stadeActuel.getLibelle()
             + " | Planté le " + datePlantation + " | Récolte prévue: " + dateRecoltePrevue
             + " | Exigences: " + exigences;
    }
}
