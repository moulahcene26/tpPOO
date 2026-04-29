package ferme.enums;

public enum StatutZone {
    ACTIVE("Active"),
    SUSPENDUE("Suspendue");

    private String libelle;

    StatutZone(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
