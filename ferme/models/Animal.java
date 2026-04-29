package ferme.models;

import ferme.enums.EtatSante;
import ferme.enums.TypeElevage;

public class Animal {
    public static final int MAX_EVENEMENTS = 50;

    private int numero;
    private String espece;
    private TypeElevage typeElevage;
    private int age;
    private double poids;
    private EtatSante etatSante;
    private PositionGPS positionActuelle;

    private String[] evenementsDates;
    private String[] evenementsDescriptions;
    private int nbEvenements;

    private double[] historiquePoids;
    private String[] historiquePoidsDate;
    private int nbHistoriquePoids;

    public Animal(int numero, String espece, TypeElevage typeElevage, int age, double poids) {
        this(numero, espece, typeElevage, age, poids, EtatSante.SAIN);
    }

    public Animal(int numero, String espece, TypeElevage typeElevage, int age, double poids, EtatSante etatSante) {
        this.numero = numero;
        this.espece = espece;
        this.typeElevage = typeElevage;
        this.age = age;
        this.poids = poids;
        this.etatSante = etatSante;
        this.positionActuelle = null;
        this.evenementsDates = new String[MAX_EVENEMENTS];
        this.evenementsDescriptions = new String[MAX_EVENEMENTS];
        this.nbEvenements = 0;
        this.historiquePoids = new double[MAX_EVENEMENTS];
        this.historiquePoidsDate = new String[MAX_EVENEMENTS];
        this.nbHistoriquePoids = 0;
    }

    public int getNumero() { return numero; }
    public String getEspece() { return espece; }
    public TypeElevage getTypeElevage() { return typeElevage; }
    public int getAge() { return age; }
    public double getPoids() { return poids; }
    public EtatSante getEtatSante() { return etatSante; }
    public PositionGPS getPositionActuelle() { return positionActuelle; }

    public void setEtatSante(EtatSante etat) { this.etatSante = etat; }
    public void setPoids(double poids) { this.poids = poids; }
    public void setPositionActuelle(PositionGPS pos) { this.positionActuelle = pos; }

    public boolean ajouterEvenementSanitaire(String date, String description) {
        if (nbEvenements >= MAX_EVENEMENTS) return false;
        evenementsDates[nbEvenements] = date;
        evenementsDescriptions[nbEvenements] = description;
        nbEvenements++;
        return true;
    }

    public boolean enregistrerPoids(String date, double nouveauPoids) {
        if (nbHistoriquePoids >= MAX_EVENEMENTS) return false;
        historiquePoidsDate[nbHistoriquePoids] = date;
        historiquePoids[nbHistoriquePoids] = nouveauPoids;
        nbHistoriquePoids++;
        this.poids = nouveauPoids;
        return true;
    }

    public void afficherEvenements() {
        System.out.println("  Événements sanitaires de l'animal #" + numero + " :");
        for (int i = 0; i < nbEvenements; i++) {
            System.out.println("    " + evenementsDates[i] + " : " + evenementsDescriptions[i]);
        }
    }

    public void afficherHistoriquePoids() {
        System.out.println("  Historique poids de l'animal #" + numero + " :");
        for (int i = 0; i < nbHistoriquePoids; i++) {
            System.out.println("    " + historiquePoidsDate[i] + " : " + historiquePoids[i] + " kg");
        }
    }

    public String toString() {
        return "Animal #" + numero + " | " + espece + " (" + typeElevage.getLibelle() + ") | Age: "
             + age + " ans | Poids: " + poids + " kg | Santé: " + etatSante.getLibelle()
             + (positionActuelle != null ? " | Position: " + positionActuelle : "");
    }
}
