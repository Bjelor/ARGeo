package cz.mendelu.argeo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.greenrobot.eventbus.EventBus;

import cz.mendelu.argeo.eventbus.OrientationMsgEvt;
import cz.mendelu.argeo.eventbus.SensorMsgEvt;
/**
 * Class initializing {@link SensorManager}, receiving sensor events via {@link SensorEventListener} and
 * calculating device orientation from accelerometer and magnetometer data. Posts {@link OrientationMsgEvt} whenever orientation is acquired,
 * as well as {@link SensorMsgEvt} whenever raw sensor data is available for gzroscope, accelerometer or magnetometer.
 */

public class OrientationHelper {

    public static final float BETA = 0.0125f;

    private String accelData = "Accelerometer Data";
    private String compassData = "Compass Data";
    private String gyroData = "Gyro Data";
    private String orientationData = "Orientation Data";

    private float[] lastAccelerometer;
    private float[] lastCompass;
    private float[] lastGyro;
    private float[] lastOrientation;

    private boolean raw = false;

    public OrientationHelper(Context context){
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                StringBuilder msg = new StringBuilder(sensorEvent.sensor.getName()).append(" ");
                for(float value: sensorEvent.values)
                {
                    msg.append("[").append(value).append("]");
                }

                switch(sensorEvent.sensor.getType())
                {
                    case Sensor.TYPE_ACCELEROMETER:
                        accelData = msg.toString();
                        lastAccelerometer = sensorEvent.values.clone();
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        gyroData = msg.toString();
                        lastGyro = sensorEvent.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        compassData = msg.toString();
                        lastCompass = sensorEvent.values.clone();
                        break;
                }

                EventBus.getDefault().post(SensorMsgEvt.generate(sensorEvent.sensor.getType(), sensorEvent.values));

                float[] vector = getOrientationVector(false);
                if(vector != null)
                    EventBus.getDefault().post(OrientationMsgEvt.generate(vector));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                //empty
            }
        };

        boolean isAccelAvailable = sensors.registerListener(sensorEventListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isCompassAvailable = sensors.registerListener(sensorEventListener, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isGyroAvailable = sensors.registerListener(sensorEventListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public float[] getOrientationVector(boolean raw){

        float rotation[] = new float[9];
        float identity[] = new float[9];

        float cameraRotation[] = new float[9];
        float orientation[] = new float[3];

        boolean gotRotation;
        try {
            gotRotation = SensorManager.getRotationMatrix(rotation,
                    identity, lastAccelerometer, lastCompass);
        } catch (NullPointerException e) {
            gotRotation = false;
        }

        if (gotRotation) {

            // remap such that the camera is pointing straight down the Y axis
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, cameraRotation);

            // orientation vector

            SensorManager.getOrientation(cameraRotation, orientation);

            if(raw)
                return orientation;

            lastOrientation = lowPass(orientation, lastOrientation);

            return lastOrientation;
        }
        return null;
    }

    public float[] lowPass(float[] input, float[] output) {
        if (isRaw())
            return input;

        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + BETA * (input[i] - output[i]);
        }
        return output;
    }

    public boolean isRaw() {
        return raw;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }
}
