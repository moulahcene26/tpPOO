package ferme.enums;

public enum FamilleCulture {
    CEREALE("Céréale"),
    LEGUME("Légume"),
    FRUIT("Fruit");

    private String libelle;

    FamilleCulture(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
