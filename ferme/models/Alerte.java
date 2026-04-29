package ferme.models;

import ferme.enums.NiveauGravite;

public class Alerte {
    private static int compteurId = 0;

    private int id;
    private Releve releve;
    private NiveauGravite niveau;
    private String codeZone;
    private boolean acquittee;
    private String dateCreation;

    public Alerte(Releve releve, NiveauGravite niveau, String codeZone) {
        this.id = ++compteurId;
        this.releve = releve;
        this.niveau = niveau;
        this.codeZone = codeZone;
        this.acquittee = false;
        this.dateCreation = releve.getDate();
    }

    public int getId() { return id; }
    public Releve getReleve() { return releve; }
    public NiveauGravite getNiveau() { return niveau; }
    public String getCodeZone() { return codeZone; }
    public boolean estAcquittee() { return acquittee; }
    public String getDateCreation() { return dateCreation; }

    public void acquitter() { this.acquittee = true; }

    public String toString() {
        String statut = acquittee ? "[ACQUITTÉE]" : "[ACTIVE]";
        return statut + " Alerte #" + id + " | " + niveau.getLibelle()
             + " | Zone: " + codeZone + " | " + releve;
    }
}
