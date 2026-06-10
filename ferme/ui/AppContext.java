package ferme.ui;

import ferme.Ferme;

public class AppContext {
    private Ferme ferme;
    private String selectedZoneCode;

    public Ferme getFerme() {
        return ferme;
    }

    public void setFerme(Ferme ferme) {
        this.ferme = ferme;
    }

    public boolean hasFerme() {
        return ferme != null;
    }

    public Ferme requireFerme() {
        if (ferme == null) {
            throw new IllegalStateException("No farm is currently loaded.");
        }
        return ferme;
    }

    public String getSelectedZoneCode() {
        return selectedZoneCode;
    }

    public void setSelectedZoneCode(String selectedZoneCode) {
        this.selectedZoneCode = selectedZoneCode;
    }
}
