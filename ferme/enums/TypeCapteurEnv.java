package ferme.enums;

public enum TypeCapteurEnv {
    TEMPERATURE("Température", "°C"),
    HUMIDITE("Humidité", "%"),
    PLUVIOMETRIE("Pluviométrie", "mm");

    private String libelle;
    private String unite;

    TypeCapteurEnv(String libelle, String unite) {
        this.libelle = libelle;
        this.unite = unite;
    }

    public String getLibelle() { return libelle; }
    public String getUnite() { return unite; }
}
