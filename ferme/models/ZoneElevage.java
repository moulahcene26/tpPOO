package ferme.models;

import ferme.enums.TypeElevage;

public class ZoneElevage extends Zone {
    public static final int MAX_ANIMAUX = 100;

    private TypeElevage typeElevage;
    private Animal[] animaux;
    private int nbAnimaux;
    private ProgrammeAlimentation programmeAlimentation;
    private PositionGPS centreZone;
    private double rayonZoneMetres;

    public ZoneElevage(String code, String nom, TypeElevage typeElevage,
                       PositionGPS centreZone, double rayonZoneMetres) {
        super(code, nom, typeElevage == TypeElevage.RUMINANT ? "litres" : "oeufs");
        this.typeElevage = typeElevage;
        this.animaux = new Animal[MAX_ANIMAUX];
        this.nbAnimaux = 0;
        this.programmeAlimentation = null;
        this.centreZone = centreZone;
        this.rayonZoneMetres = rayonZoneMetres;
    }

    public boolean ajouterAnimal(Animal animal) {
        if (nbAnimaux >= MAX_ANIMAUX) return false;
        if (animal.getTypeElevage() != this.typeElevage) {
            System.out.println("  Erreur : type d'élevage incompatible.");
            return false;
        }
        animaux[nbAnimaux] = animal;
        nbAnimaux++;
        return true;
    }

    public Animal getAnimal(int index) {
        if (index >= 0 && index < nbAnimaux) return animaux[index];
        return null;
    }

    public Animal rechercherAnimalParNumero(int numero) {
        for (int i = 0; i < nbAnimaux; i++) {
            if (animaux[i].getNumero() == numero) return animaux[i];
        }
        return null;
    }

    public int getNbAnimaux() { return nbAnimaux; }
    public TypeElevage getTypeElevage() { return typeElevage; }
    public ProgrammeAlimentation getProgrammeAlimentation() { return programmeAlimentation; }
    public PositionGPS getCentreZone() { return centreZone; }
    public double getRayonZoneMetres() { return rayonZoneMetres; }

    public void setProgrammeAlimentation(ProgrammeAlimentation programme) {
        this.programmeAlimentation = programme;
    }

    public String getTypeZone() { return "Élevage (" + typeElevage.getLibelle() + ")"; }

    public int getNombreEntites() { return nbAnimaux; }

    public void afficherDetails() {
        System.out.println("=== Zone d'Élevage : " + nom + " (" + code + ") ===");
        System.out.println("Type : " + typeElevage.getLibelle());
        System.out.println("Statut : " + statut.getLibelle());
        System.out.println("Nombre d'animaux : " + nbAnimaux);
        for (int i = 0; i < nbAnimaux; i++) {
            System.out.println("  " + (i + 1) + ". " + animaux[i]);
        }
        if (programmeAlimentation != null) {
            System.out.println("Programme d'alimentation : " + programmeAlimentation);
        }
        historiqueProduction.afficher();
    }
}
