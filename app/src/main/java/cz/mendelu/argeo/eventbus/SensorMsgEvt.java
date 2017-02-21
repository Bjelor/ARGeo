package cz.mendelu.argeo.eventbus;

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
}
