package ferme.enums;

public enum TypeCapteurEau {
    TEMPERATURE_EAU("Température eau", "°C"),
    OXYGENE_DISSOUS("Oxygène dissous", "mg/L"),
    PH_EAU("pH eau", "");

    private String libelle;
    private String unite;

    TypeCapteurEau(String libelle, String unite) {
        this.libelle = libelle;
        this.unite = unite;
    }

    public String getLibelle() { return libelle; }
    public String getUnite() { return unite; }
}
