package ferme.models;

import ferme.enums.TypeElevage;
import java.util.LinkedHashMap;
import java.util.Map;

public class ZoneElevage extends Zone {

    private TypeElevage typeElevage;
    private Map<Integer, Animal> animaux;
    private ProgrammeAlimentation programmeAlimentation;
    private PositionGPS centreZone;
    private double rayonZoneMetres;

    public ZoneElevage(String code, String nom, TypeElevage typeElevage,
                       PositionGPS centreZone, double rayonZoneMetres) {
        super(code, nom, typeElevage == TypeElevage.RUMINANT ? "litres" : "oeufs");
        this.typeElevage = typeElevage;
        this.animaux = new LinkedHashMap<>();
        this.programmeAlimentation = null;
        this.centreZone = centreZone;
        this.rayonZoneMetres = rayonZoneMetres;
    }

    public boolean ajouterAnimal(Animal animal) {
        if (animal.getTypeElevage() != this.typeElevage) {
            System.out.println("  Erreur : type d'élevage incompatible.");
            return false;
        }
        animaux.put(animal.getNumero(), animal);
        return true;
    }

    public Animal rechercherAnimalParNumero(int numero) {
        return animaux.get(numero);
    }

    public int getNbAnimaux() { return animaux.size(); }
    public TypeElevage getTypeElevage() { return typeElevage; }
    public ProgrammeAlimentation getProgrammeAlimentation() { return programmeAlimentation; }
    public PositionGPS getCentreZone() { return centreZone; }
    public double getRayonZoneMetres() { return rayonZoneMetres; }

    public void setProgrammeAlimentation(ProgrammeAlimentation programme) {
        this.programmeAlimentation = programme;
    }

    public String getTypeZone() { return "Élevage (" + typeElevage.getLibelle() + ")"; }

    public int getNombreEntites() { return animaux.size(); }

    public void afficherDetails() {
        System.out.println("=== Zone d'Élevage : " + nom + " (" + code + ") ===");
        System.out.println("Type : " + typeElevage.getLibelle());
        System.out.println("Statut : " + statut.getLibelle());
        System.out.println("Nombre d'animaux : " + animaux.size());
        int i = 1;
        for (Animal a : animaux.values()) {
            System.out.println("  " + i++ + ". " + a);
        }
        if (programmeAlimentation != null) {
            System.out.println("Programme d'alimentation : " + programmeAlimentation);
        }
        historiqueProduction.afficher();
    }
}
