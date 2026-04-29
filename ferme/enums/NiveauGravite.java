package ferme.enums;

public enum NiveauGravite {
    NORMAL("Normal"),
    AVERTISSEMENT("Avertissement"),
    CRITIQUE("Critique");

    private String libelle;

    NiveauGravite(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
