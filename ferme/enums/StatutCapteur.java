package ferme.enums;

public enum StatutCapteur {
    ACTIF("Actif"),
    DEFAILLANT("Défaillant"),
    SUSPENDU("Suspendu");

    private String libelle;

    StatutCapteur(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
