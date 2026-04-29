package ferme.interfaces;

import ferme.enums.NiveauGravite;

public interface IGestionAlertes {
    void afficherPanneauAlertes();
    void afficherAlertesActives();
    boolean acquitterAlerte(int idAlerte);
    boolean supprimerAlerte(int idAlerte);
    void consulterHistoriqueAlertes(String codeZone, NiveauGravite niveau, String typeCapteur, String dateDebut, String dateFin);
    void trierAlertesParGravite();
}
