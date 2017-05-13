package cz.mendelu.argeo.eventbus;

import java.util.Locale;

/**
 * Created by Bjelis on 12.5.2017.
 */

public class OrientationMsgEvt {
    private float[] values;

    public OrientationMsgEvt(float[] values) {
        this.values = values;
    }

    public static OrientationMsgEvt generate(float[] values){
        return new OrientationMsgEvt(values);
    }

    public float[] getValues() {
        return values;
    }

    public float getCurrentBearing(){
        float bearing = (float) (values[0] * 180 / Math.PI);
        if (bearing < 0) {
            bearing += 360;
        }

        return bearing;
    }

    @Override
    public String toString() {
        String string = OrientationMsgEvt.class.getSimpleName();
        for(int i = 0; i<values.length; i++){
            string = string + " " + String.format(Locale.getDefault(),"%.2f",values[i]);
        }
        return string;
    }
}
