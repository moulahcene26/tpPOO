package ferme.interfaces;

import ferme.models.Capteur;
import ferme.models.Releve;

public interface IGestionCapteurs {
    boolean ajouterCapteur(Capteur capteur);
    boolean configurerCapteur(String codeCapteur, double seuilMin, double seuilMax);
    boolean changerStatutCapteur(String codeCapteur, ferme.enums.StatutCapteur nouveauStatut);
    void afficherTableauDeBord(String codeZone);
    void consulterHistoriqueReleves(String codeCapteur, String dateDebut, String dateFin);
    boolean enregistrerReleve(String codeCapteur, Releve releve);
    void afficherGraphiqueEvolution(String codeCapteur);
    void afficherGraphiqueParZone(String codeZone);
}
