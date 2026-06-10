package ferme.models;

import java.util.ArrayList;
import java.util.List;

public class ZoneAquacole extends Zone {

    private List<EspeceAquacole> especes;
    private ProgrammeAlimentation programmeAlimentation;

    public ZoneAquacole(String code, String nom) {
        super(code, nom, "kg");
        this.especes = new ArrayList<>();
        this.programmeAlimentation = null;
    }

    public boolean ajouterEspece(EspeceAquacole espece) {
        if (especes.contains(espece))
            return false;
        especes.add(espece);
        return true;
    }

    public EspeceAquacole getEspece(int index) {
        if (index >= 0 && index < especes.size())
            return especes.get(index);
        return null;
    }

    public int getNbEspeces() {
        return especes.size();
    }

    public ProgrammeAlimentation getProgrammeAlimentation() {
        return programmeAlimentation;
    }

    public List<EspeceAquacole> getEspeces() {
        return new ArrayList<>(especes);
    }

    public void setProgrammeAlimentation(ProgrammeAlimentation programme) {
        this.programmeAlimentation = programme;
    }

    public int getNombreTotalAnimaux() {
        int total = 0;
        for (int i = 0; i < especes.size(); i++) {
            total += especes.get(i).getNombreAnimaux();
        }
        return total;
    }

    public String getTypeZone() {
        return "Aquacole";
    }

    public int getNombreEntites() {
        return getNombreTotalAnimaux();
    }

    public void afficherDetails() {
        System.out.println("=== Zone Aquacole : " + nom + " (" + code + ") ===");
        System.out.println("Statut : " + statut.getLibelle());
        System.out.println("Espèces (" + especes.size() + ") :");
        for (int i = 0; i < especes.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + especes.get(i));
        }
        System.out.println("Nombre total d'animaux : " + getNombreTotalAnimaux());
        if (programmeAlimentation != null) {
            System.out.println("Programme d'alimentation : " + programmeAlimentation);
        }
        historiqueProduction.afficher();
    }
}
