package ferme.enums;

public enum TypeCapteurSol {
    PH("pH", ""),
    HUMIDITE_SOL("Humidité du sol", "%"),
    AZOTE("Teneur en azote", "mg/kg");

    private String libelle;
    private String unite;

    TypeCapteurSol(String libelle, String unite) {
        this.libelle = libelle;
        this.unite = unite;
    }

    public String getLibelle() { return libelle; }
    public String getUnite() { return unite; }
}
