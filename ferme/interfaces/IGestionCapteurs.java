package ferme.interfaces;

import ferme.models.Capteur;
import ferme.models.Releve;

public interface IGestionCapteurs {
    boolean ajouterCapteur(Capteur capteur);
    boolean configurerCapteur(String codeCapteur, double seuilMin, double seuilMax);
    boolean changerStatutCapteur(String codeCapteur, ferme.enums.StatutCapteur nouveauStatut);
    int chargerCapteursDepuisFichier(String cheminFichier);
    int chargerCapteursDepuisFichiers(String[] cheminsFichiers);
    int chargerRelevesDepuisFichier(String cheminFichier);
    int chargerRelevesDepuisFichiers(String[] cheminsFichiers);
    void afficherTableauDeBord(String codeZone);
    void consulterHistoriqueReleves(String codeCapteur, String dateDebut, String dateFin);
    boolean enregistrerReleve(String codeCapteur, Releve releve);

}
