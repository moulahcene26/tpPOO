package ferme.interfaces;

import ferme.models.Zone;
import ferme.models.Culture;
import ferme.models.Animal;

public interface IGestionZones {
    boolean ajouterZone(Zone zone);
    boolean modifierZone(String codeZone, String nouveauNom);
    boolean activerZone(String codeZone);
    boolean suspendreZone(String codeZone);
    Zone rechercherZone(String codeZone);
    boolean affecterCultureAZone(String codeZone, Culture culture);
    boolean affecterAnimalAZone(String codeZone, Animal animal);
    void afficherVueEnsembleZones();
    void enregistrerProduction(String codeZone, double valeur, String date);
}
