package ferme.interfaces;

import ferme.models.Culture;
import ferme.enums.FamilleCulture;
import ferme.enums.StadeCroissance;

public interface IGestionCultures {
    boolean enregistrerCulture(Culture culture);
    boolean mettreAJourStade(String codeZone, int indexCulture, StadeCroissance nouveauStade);
    void afficherStadeCroissance(String codeZone);
    void genererRapportCulturesParZone(String codeZone);
    void afficherCulturesParFamille(String codeZone, FamilleCulture famille);
}
