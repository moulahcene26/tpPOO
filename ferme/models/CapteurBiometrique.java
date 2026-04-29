package ferme.models;

import ferme.enums.TypeCapteurBio;

public class CapteurBiometrique extends Capteur {
    private TypeCapteurBio type;
    private int numeroAnimal;

    public CapteurBiometrique(String code, String codeZone, double seuilMin, double seuilMax,
                               TypeCapteurBio type, int numeroAnimal) {
        super(code, codeZone, seuilMin, seuilMax);
        this.type = type;
        this.numeroAnimal = numeroAnimal;
    }

    public TypeCapteurBio getType() { return type; }
    public int getNumeroAnimal() { return numeroAnimal; }

    public String getTypeCapteur() { return "Biométrique - " + type.getLibelle(); }
    public String getUnite() { return type.getUnite(); }
}
