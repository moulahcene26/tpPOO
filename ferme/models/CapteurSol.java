package ferme.models;

import ferme.enums.TypeCapteurSol;

public class CapteurSol extends Capteur {
    private TypeCapteurSol type;

    public CapteurSol(String code, String codeZone, double seuilMin, double seuilMax, TypeCapteurSol type) {
        super(code, codeZone, seuilMin, seuilMax);
        this.type = type;
    }

    public TypeCapteurSol getType() { return type; }

    public String getTypeCapteur() { return "Sol - " + type.getLibelle(); }
    public String getUnite() { return type.getUnite(); }
}
