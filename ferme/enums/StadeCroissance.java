package ferme.enums;

public enum StadeCroissance {
    SEMIS("Semis"),
    GERMINATION("Germination"),
    CROISSANCE("Croissance"),
    MATURITE("Maturité"),
    RECOLTE("Récolte");

    private String libelle;

    StadeCroissance(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
