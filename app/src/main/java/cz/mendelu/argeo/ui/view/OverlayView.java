package cz.mendelu.argeo.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import cz.mendelu.argeo.eventbus.SensorMsgEvt;
import cz.mendelu.argeo.util.ARLog;

/**
 * A temporary overlay providing virtual data. Will be replaced by something more suitable in the future.
 * @author adamb_000
 * @since 13. 7. 2016
 */
    //TODO: this class appears unnecessary - use textviews for sensor data if needed
    //TODO: LocationManager should probably be located in an Activity
    //FIXME: bad behavior on screen orientation change
public class OverlayView extends View {

    // ========================================================================
    // =====================   C  O  N  S  T  A  N  T  S   ====================
    // ========================================================================

    public static final String TAG = OverlayView.class.getSimpleName();

    /**
     * Faculty of Mechanical Engineering location
     */
    static final Location vut = new Location("manual");
    static {
        vut.setLatitude(49.224278d);
        vut.setLongitude(16.578444d);
        vut.setAltitude(450.5d);
    }

    final float BETA = 0.1f;
    final Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ========================================================================
    // ========================   M  E  M  B  E  R  S   =======================
    // ========================================================================

    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";

    Location lastLocation = null;
    LocationManager locationManager;

//    float[] rotation = new float[9];
//    float[] identity = new float[9];
//    float[] cameraRotation = new float[9];
//    float[] orientation = new float[9];

    float[] lastAccelerometer;
    float[] lastCompass;
    float[] lastGyro;


    // ========================================================================
    // =======================    M  E  T  H  O  D  S   =======================
    // ========================================================================

    public OverlayView(Context context) {
        super(context);

        targetPaint.setColor(0xFF00FF00);
        contentPaint.setTextAlign(Paint.Align.CENTER);
        contentPaint.setTextSize(20);
        contentPaint.setColor(Color.RED);

        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        boolean isAccelAvailable = sensors.registerListener(mSensorEventListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isCompassAvailable = sensors.registerListener(mSensorEventListener, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isGyroAvailable = sensors.registerListener(mSensorEventListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        ARLog.v("[%s]::[Best provider: %s]", TAG, best);

        //FIXME: move LocationManager to an Activity?
        try {
            locationManager.requestLocationUpdates(best, 50, 0, mLocationListener);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(best);
            }
        } catch (SecurityException e){
            ARLog.e("[%s]::[SecurityException: %s]", TAG, e.getMessage());
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Arrays.fill(rotation, 0.0f);
//        Arrays.fill(cameraRotation, 0.0f);
//        Arrays.fill(orientation, 0.0f);
//        Arrays.fill(identity, 0.0f);


        canvas.drawText(accelData, canvas.getWidth() / 2, canvas.getHeight() / 4, contentPaint);
        canvas.drawText(compassData, canvas.getWidth() / 2, canvas.getHeight() / 2, contentPaint);
        canvas.drawText(gyroData, canvas.getWidth() / 2, (canvas.getHeight() * 3) / 4, contentPaint);

        float rotation[] = new float[9];
        float identity[] = new float[9];

        if (ArDisplayView.getCamera() == null){
            ARLog.e("[%s]::[camera was null]",TAG);
            return;
        }

        float verticalFOV = ArDisplayView.getCamera().getVerticalAngle();

        boolean gotRotation;

        try {
            gotRotation = SensorManager.getRotationMatrix(rotation,
                    identity, lastAccelerometer, lastCompass);
        } catch (NullPointerException e) {
            gotRotation = false;
        }

        if (gotRotation) {
            float cameraRotation[] = new float[9];
            // remap such that the camera is pointing straight down the Y axis
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, cameraRotation);

            // orientation vector
            float orientation[] = new float[3];
            SensorManager.getOrientation(cameraRotation, orientation);

            if(lastLocation == null){
                ARLog.e("[%s]::[lastLocation was null]",TAG);
                return;
            }

            float curBearingToMW = lastLocation.bearingTo(vut);
            float horizontalFOV = ArDisplayView.getCamera().getHorizontalAngle();
//            ARLog.d("[%s]::[v: %s, h:%s]", TAG, verticalFOV, horizontalFOV);

            // use roll for screen rotation
            canvas.rotate((float)(0.0f- Math.toDegrees(orientation[2])));
            // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
            float dx = (float) ( (canvas.getWidth()/ horizontalFOV) * (Math.toDegrees(orientation[0])-curBearingToMW));
            float dy = (float) ( (canvas.getHeight()/ verticalFOV) * Math.toDegrees(orientation[1])) ;

            // wait to translate the dx so the horizon doesn't get pushed off
            canvas.translate(0.0f, 0.0f-dy);

            // make our line big enough to draw regardless of rotation and translation
            canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, targetPaint);

            // now translate the dx
            canvas.translate(0.0f-dx, 0.0f);

            // draw our point -- we've rotated and translated this to the right spot already
            canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 8.0f, contentPaint);
        }
    }

    //TODO this will be requiring some tweaks as the app is slow to respond to sensor changes
    private float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + BETA * (input[i] - output[i]);
        }
        return output;
    }

    // ========================================================================
    // =====================   L  I  S  T  E  N  E  R  S   ====================
    // ========================================================================

    SensorEventListener mSensorEventListener = new SensorEventListener() {
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
                    lastAccelerometer = lowPass(sensorEvent.values.clone(), lastAccelerometer);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyroData = msg.toString();
//                    lastGyro = sensorEvent.values;
                    lastGyro = lowPass(sensorEvent.values.clone(), lastGyro);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    compassData = msg.toString();
//                    lastCompass = sensorEvent.values;
                    lastCompass = lowPass(sensorEvent.values.clone(), lastCompass);
                    break;
            }

            EventBus.getDefault().post(SensorMsgEvt.generate(sensorEvent.sensor.getType(), sensorEvent.values));

            OverlayView.this.invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //empty
        }
    };

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            //empty
        }

        @Override
        public void onProviderEnabled(String s) {
            //empty
        }

        @Override
        public void onProviderDisabled(String s) {
            //empty
        }
    };

}