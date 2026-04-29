package ferme.models;

import ferme.enums.TypeCapteurEau;

public class CapteurEau extends Capteur {
    private TypeCapteurEau type;

    public CapteurEau(String code, String codeZone, double seuilMin, double seuilMax, TypeCapteurEau type) {
        super(code, codeZone, seuilMin, seuilMax);
        this.type = type;
    }

    public TypeCapteurEau getType() { return type; }

    public String getTypeCapteur() { return "Eau - " + type.getLibelle(); }
    public String getUnite() { return type.getUnite(); }
}
