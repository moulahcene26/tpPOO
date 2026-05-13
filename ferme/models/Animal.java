package ferme.models;

import ferme.enums.EtatSante;
import ferme.enums.TypeElevage;
import java.util.ArrayList;
import java.util.List;

public class Animal {

    private int numero;
    private String espece;
    private TypeElevage typeElevage;
    private int age;
    private double poids;
    private EtatSante etatSante;
    private PositionGPS positionActuelle;

    private List<String> evenementsDates;
    private List<String> evenementsDescriptions;
    private List<String> historiquePoidsDate;
    private List<Double> historiquePoids;

    public Animal(int numero, String espece, TypeElevage typeElevage, int age, double poids) {
        this(numero, espece, typeElevage, age, poids, EtatSante.SAIN);
    }

    public Animal(int numero, String espece, TypeElevage typeElevage, int age, double poids, EtatSante etatSante) {
        this.numero = numero;
        this.espece = espece;
        this.typeElevage = typeElevage;
        this.age = age;
        if (poids < 0) throw new IllegalArgumentException("Poids invalide");
        this.poids = poids;
        this.etatSante = etatSante;
        this.positionActuelle = null;
        this.evenementsDates = new ArrayList<>();
        this.evenementsDescriptions = new ArrayList<>();
        this.historiquePoidsDate = new ArrayList<>();
        this.historiquePoids = new ArrayList<>();
    }

    public int getNumero() { return numero; }
    public String getEspece() { return espece; }
    public TypeElevage getTypeElevage() { return typeElevage; }
    public int getAge() { return age; }
    public double getPoids() { return poids; }
    public EtatSante getEtatSante() { return etatSante; }
    public PositionGPS getPositionActuelle() { return positionActuelle; }

    public void setEtatSante(EtatSante etat) { this.etatSante = etat; }
    public void setPoids(double poids) { if (poids < 0) throw new IllegalArgumentException("Poids invalide"); this.poids = poids; }
    public void setPositionActuelle(PositionGPS pos) { this.positionActuelle = pos; }

    public boolean ajouterEvenementSanitaire(String date, String description) {
        evenementsDates.add(date);
        evenementsDescriptions.add(description);
        return true;
    }

    public boolean enregistrerPoids(String date, double nouveauPoids) {
        if (nouveauPoids < 0) throw new IllegalArgumentException("Poids invalide");
        historiquePoidsDate.add(date);
        historiquePoids.add(nouveauPoids);
        this.poids = nouveauPoids;
        return true;
    }

    public void afficherEvenements() {
        System.out.println("  Événements sanitaires de l'animal #" + numero + " :");
        for (int i = 0; i < evenementsDates.size(); i++) {
            System.out.println("    " + evenementsDates.get(i) + " : " + evenementsDescriptions.get(i));
        }
    }

    public void afficherHistoriquePoids() {
        System.out.println("  Historique poids de l'animal #" + numero + " :");
        for (int i = 0; i < historiquePoidsDate.size(); i++) {
            System.out.println("    " + historiquePoidsDate.get(i) + " : " + historiquePoids.get(i) + " kg");
        }
    }

    public String toString() {
        return "Animal #" + numero + " | " + espece + " (" + typeElevage.getLibelle() + ") | Age: "
             + age + " ans | Poids: " + poids + " kg | Santé: " + etatSante.getLibelle()
             + (positionActuelle != null ? " | Position: " + positionActuelle : "");
    }
}
