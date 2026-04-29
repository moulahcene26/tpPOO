package ferme.models;

public class ZoneAquacole extends Zone {
    public static final int MAX_ESPECES = 20;

    private EspeceAquacole[] especes;
    private int nbEspeces;
    private ProgrammeAlimentation programmeAlimentation;

    public ZoneAquacole(String code, String nom) {
        super(code, nom, "kg");
        this.especes = new EspeceAquacole[MAX_ESPECES];
        this.nbEspeces = 0;
        this.programmeAlimentation = null;
    }

    public boolean ajouterEspece(EspeceAquacole espece) {
        if (nbEspeces >= MAX_ESPECES) return false;
        especes[nbEspeces] = espece;
        nbEspeces++;
        return true;
    }

    public EspeceAquacole getEspece(int index) {
        if (index >= 0 && index < nbEspeces) return especes[index];
        return null;
    }

    public int getNbEspeces() { return nbEspeces; }
    public ProgrammeAlimentation getProgrammeAlimentation() { return programmeAlimentation; }

    public void setProgrammeAlimentation(ProgrammeAlimentation programme) {
        this.programmeAlimentation = programme;
    }

    public int getNombreTotalAnimaux() {
        int total = 0;
        for (int i = 0; i < nbEspeces; i++) {
            total += especes[i].getNombreAnimaux();
        }
        return total;
    }

    public String getTypeZone() { return "Aquacole"; }

    public int getNombreEntites() { return getNombreTotalAnimaux(); }

    public void afficherDetails() {
        System.out.println("=== Zone Aquacole : " + nom + " (" + code + ") ===");
        System.out.println("Statut : " + statut.getLibelle());
        System.out.println("Espèces (" + nbEspeces + ") :");
        for (int i = 0; i < nbEspeces; i++) {
            System.out.println("  " + (i + 1) + ". " + especes[i]);
        }
        System.out.println("Nombre total d'animaux : " + getNombreTotalAnimaux());
        if (programmeAlimentation != null) {
            System.out.println("Programme d'alimentation : " + programmeAlimentation);
        }
        historiqueProduction.afficher();
    }
}
