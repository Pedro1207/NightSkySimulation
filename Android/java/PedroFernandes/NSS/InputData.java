package PedroFernandes.NSS;

public class InputData {

    float value;
    String strValue;
    boolean integer;
    String name;
    float division;
    float offset;

    public InputData(float value, String strValue, boolean integer, String name, float division, float offset) {
        this.value = value;
        this.strValue = strValue;
        this.integer = integer;
        this.name = name;
        this.division = division;
        this.offset = offset;
    }

    float getValue(){
        return this.value / this.division;
    }

    String getStrValue(){
        return strValue;
    }
}
