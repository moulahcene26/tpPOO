package ferme.models;

import ferme.enums.StatutCapteur;
import ferme.enums.StatutZone;
import java.util.ArrayList;
import java.util.List;

public abstract class Zone {
    protected String code;
    protected String nom;
    protected StatutZone statut;
    protected HistoriqueProduction historiqueProduction;
    protected List<Capteur> capteursAssoc;

    public Zone(String code, String nom, String uniteProduction) {
        this.code = code;
        this.nom = nom;
        this.statut = StatutZone.ACTIVE;
        this.historiqueProduction = new HistoriqueProduction(uniteProduction);
        this.capteursAssoc = new ArrayList<>();
    }

    public String getCode() { return code; }
    public String getNom() { return nom; }
    public StatutZone getStatut() { return statut; }
    public HistoriqueProduction getHistoriqueProduction() { return historiqueProduction; }

    public void setNom(String nom) { this.nom = nom; }

    public void activer() {
        this.statut = StatutZone.ACTIVE;
        for (Capteur c : capteursAssoc) {
            if( (c != null) && (c.getStatut() != StatutCapteur.DEFAILLANT) ) {
                c.reactiver();
            }
        }
    }

    public void suspendre() {
        this.statut = StatutZone.SUSPENDUE;
        for (Capteur c : capteursAssoc) {
            if (c != null) c.suspendre();
        }
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

    public void ajouterCapteurAssoc(Capteur c) {
        if (c == null) return;
        if (!capteursAssoc.contains(c)) capteursAssoc.add(c);
    }

    public void retirerCapteurAssoc(Capteur c) {
        if (c == null) return;
        capteursAssoc.remove(c);
    }

    
}
