package ferme;

public enum TypeAnimaux {
    RUMINANTS("vache, mouton, chèvre"),
    VOLAILLE("poulet, dinde");

    private String libelle;

    TypeAnimaux(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}

