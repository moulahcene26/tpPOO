package ferme.enums;

public enum TypeCapteurBio {
    TEMPERATURE_CORPORELLE("Température corporelle", "°C"),
    ACTIVITE("Niveau d'activité", "pas/min");

    private String libelle;
    private String unite;

    TypeCapteurBio(String libelle, String unite) {
        this.libelle = libelle;
        this.unite = unite;
    }

    public String getLibelle() { return libelle; }
    public String getUnite() { return unite; }
}
