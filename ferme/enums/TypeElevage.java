package ferme.enums;

public enum TypeElevage {
    RUMINANT("Ruminant"),
    VOLAILLE("Volaille");

    private String libelle;

    TypeElevage(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
