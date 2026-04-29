package ferme.models;

import ferme.enums.StatutZone;

public abstract class Zone {
    protected String code;
    protected String nom;
    protected StatutZone statut;
    protected HistoriqueProduction historiqueProduction;

    public Zone(String code, String nom, String uniteProduction) {
        this.code = code;
        this.nom = nom;
        this.statut = StatutZone.ACTIVE;
        this.historiqueProduction = new HistoriqueProduction(uniteProduction);
    }

    public String getCode() { return code; }
    public String getNom() { return nom; }
    public StatutZone getStatut() { return statut; }
    public HistoriqueProduction getHistoriqueProduction() { return historiqueProduction; }

    public void setNom(String nom) { this.nom = nom; }

    public void activer() {
        this.statut = StatutZone.ACTIVE;
    }

    public void suspendre() {
        this.statut = StatutZone.SUSPENDUE;
    }

    public void enregistrerProduction(double valeur, String date) {
        historiqueProduction.ajouterEntree(date, valeur);
    }

    public abstract String getTypeZone();
    public abstract int getNombreEntites();
    public abstract void afficherDetails();

    public String toString() {
        return "[" + getTypeZone() + "] " + code + " - " + nom
             + " | Statut: " + statut.getLibelle()
             + " | Entités: " + getNombreEntites();
    }
}
