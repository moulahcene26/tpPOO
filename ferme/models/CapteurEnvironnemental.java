package ferme.models;

import ferme.enums.TypeCapteurEnv;

public class CapteurEnvironnemental extends Capteur {
    private TypeCapteurEnv type;

    public CapteurEnvironnemental(String code, String codeZone, double seuilMin, double seuilMax, TypeCapteurEnv type) {
        super(code, codeZone, seuilMin, seuilMax);
        this.type = type;
    }

    public TypeCapteurEnv getType() { return type; }

    public String getTypeCapteur() { return "Environnemental - " + type.getLibelle(); }
    public String getUnite() { return type.getUnite(); }
}
