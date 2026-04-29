package ferme.models;

public class EspeceAquacole {
    private String nom;
    private int nombreAnimaux;

    public EspeceAquacole(String nom, int nombreAnimaux) {
        this.nom = nom;
        this.nombreAnimaux = nombreAnimaux;
    }

    public String getNom() { return nom; }
    public int getNombreAnimaux() { return nombreAnimaux; }

    public void setNombreAnimaux(int n) { this.nombreAnimaux = n; }

    public String toString() {
        return nom + " (x" + nombreAnimaux + ")";
    }
}
