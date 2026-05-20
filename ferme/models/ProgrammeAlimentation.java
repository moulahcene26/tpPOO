package ferme.models;

public class ProgrammeAlimentation {
    private String typeAliment;
    private double quantiteParRepas;
  

    public ProgrammeAlimentation(String typeAliment, double quantiteParRepas, int nombreRepasParJour) {
        this.typeAliment = typeAliment;
        this.quantiteParRepas = quantiteParRepas;
      
    }

    public String getTypeAliment() { return typeAliment; }
    public double getQuantiteParRepas() { return quantiteParRepas; }


    public void setTypeAliment(String typeAliment) { this.typeAliment = typeAliment; }
    public void setQuantiteParRepas(double quantiteParRepas) { this.quantiteParRepas = quantiteParRepas; }



    public String toString() {
        return "Aliment: " + typeAliment + ", " + quantiteParRepas + " kg/repas, " ;
    }
}
