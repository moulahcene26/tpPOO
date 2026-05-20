package ferme.models;

import ferme.ValidationUtils;
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
        this.datePlantation = ValidationUtils.validerDateSimple(datePlantation, "date de plantation");
        this.dateRecoltePrevue = ValidationUtils.validerDateSimple(dateRecoltePrevue, "date de récolte prévue");
        if (this.dateRecoltePrevue.compareTo(this.datePlantation) < 0) {
            throw new IllegalArgumentException("La date de récolte prévue doit être postérieure à la date de plantation.");
        }
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

    @Override
    public String toString() {
        return nom + " (" + famille.getLibelle() + ") | Stade: " + stadeActuel.getLibelle() 
             + " | Récolte: " + dateRecoltePrevue;
    }
}
