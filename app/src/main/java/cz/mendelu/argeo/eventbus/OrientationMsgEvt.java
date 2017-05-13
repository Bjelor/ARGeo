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

    @Override
    public String toString() {
        String string = OrientationMsgEvt.class.getSimpleName();
        for(int i = 0; i<values.length; i++){
            string = string + " " + String.format(Locale.getDefault(),"%.2f",values[i]);
        }
        return string;
    }
}
