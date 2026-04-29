package ferme.models;

import ferme.enums.NiveauGravite;

public abstract class Releve {
    private String date;
    private String codeCapteur;
    private NiveauGravite niveau;

    protected Releve(String date, String codeCapteur) {
        this.date = date;
        this.codeCapteur = codeCapteur;
        this.niveau = NiveauGravite.NORMAL;
    }

    public String getDate() { return date; }
    public String getCodeCapteur() { return codeCapteur; }
    public NiveauGravite getNiveau() { return niveau; }

    public void setNiveau(NiveauGravite niveau) { this.niveau = niveau; }

    public abstract boolean estGPS();

    public double getValeur() {
        throw new UnsupportedOperationException("Ce relevé ne contient pas de valeur numérique.");
    }

    public String getUnite() {
        return "";
    }

    public PositionGPS getPosition() {
        return null;
    }
}
