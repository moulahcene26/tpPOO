package ferme.enums;

public enum EtatSante {
    SAIN("Sain"),
    MALADE("Malade"),
    QUARANTAINE("Quarantaine");

    private String libelle;

    EtatSante(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
