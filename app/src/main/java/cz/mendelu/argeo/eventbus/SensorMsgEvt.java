package cz.mendelu.argeo.eventbus;

import java.util.Locale;

/**
 * Created by Bjelis on 13.2.2017.
 */

public class SensorMsgEvt {
    private float[] values;
    private int type;

    public SensorMsgEvt(int type, float[] values) {
        this.type = type;

        this.values = values;
    }

    public static SensorMsgEvt generate(int type, float[] values){
        return new SensorMsgEvt(type, values);
    }

    public float[] getValues() {
        return values;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        String string = String.valueOf(getType());
        for(int i = 0; i<values.length; i++){
            string = string + " " + String.format(Locale.getDefault(),"%.2f",values[i]);
        }
        return string;
    }
}
