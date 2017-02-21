package cz.mendelu.argeo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.os.Environment;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;

import cz.mendelu.argeo.util.ARLog;

/**
 * Created by Bjelis on 13.2.2017.
 */

public class SensorLog {

    private static final String TAG = SensorLog.class.getSimpleName();

    private static final String ACC_LOG = "accLog";
    private static final String MAG_LOG = "magLog";
    private static final String GYR_LOG = "gyrLog";

    private static final String LOG_EXTENSION = ".csv";

    private static final String FLOAT_FORMAT_MASK = "%.8f";
    private static final int TOTAL_ITERATIONS = 100;

    private File accFile;
    private File gyrFile;
    private File magFile;

    private final HashMap<Integer, Integer> iteratorMap = new HashMap<>();
    private final HashMap<Integer, Boolean> isOpenMap = new HashMap<>();
    private boolean mShouldInitLog = true;

    private static SensorLog sInstance = null;

    private SensorLog(){
        iteratorMap.put(Sensor.TYPE_ACCELEROMETER, 0);
        iteratorMap.put(Sensor.TYPE_GYROSCOPE, 0);
        iteratorMap.put(Sensor.TYPE_MAGNETIC_FIELD, 0);

        isOpenMap.put(Sensor.TYPE_ACCELEROMETER, true);
        isOpenMap.put(Sensor.TYPE_GYROSCOPE, true);
        isOpenMap.put(Sensor.TYPE_MAGNETIC_FIELD, true);

        checkStorageAvailability();
    }

    public static SensorLog getInstance(){
        if(sInstance == null)
            sInstance = new SensorLog();

        return sInstance;
    }

    public void performLog(final int type, final float[] values, Activity context){

        if(!isLogOpen(type))
            return;

        final OutputStreamWriter osw;
        final File file;

        try {
            switch (type){
                case Sensor.TYPE_ACCELEROMETER:
                    if(mShouldInitLog || accFile == null)
                        accFile = createNewLog(context, ACC_LOG);
                    file = accFile;
                    osw = new OutputStreamWriter(new FileOutputStream(accFile, true));
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    if(mShouldInitLog || gyrFile == null)
                        gyrFile = createNewLog(context, GYR_LOG);
                    file = gyrFile;
                    osw = new OutputStreamWriter(new FileOutputStream(file, true));
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    if(mShouldInitLog || magFile == null)
                        magFile = createNewLog(context, MAG_LOG);
                    file = magFile;
                    osw = new OutputStreamWriter(new FileOutputStream(file, true));
                    break;
                default:
                    ARLog.e("[%s]::[sensor type: %s, not logging this entry]", TAG, type);
                    return;
            }

            if(mShouldInitLog){
                mShouldInitLog = false;

                ARLog.d("[%s]::[initSensorLog]", TAG);

                context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                return;
            }

            int it = getIteratorValue(type);

            if(it == 0){
                osw.write(makeDataHeader());
                incrementIterator(type);
            } else if (it < TOTAL_ITERATIONS){
                osw.write(makeDataLine(values));
                ARLog.d("[%s]::[%s]::[%s]", TAG, file.getAbsolutePath(), makeDataLine(values));
                incrementIterator(type);
            } else {
                closeLog(type);
                if(areAllLogsClosed()) {
                    context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    ARLog.d("[%s]::[Logging finished]", TAG);
                }
            }

            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String makeDataHeader(){
        return "x;y;z\n";
    }

    private String makeDataLine(float[] values){
        if(values.length == 3)
            return String.format(Locale.getDefault(),FLOAT_FORMAT_MASK,values[0])
                    + ";" + String.format(Locale.getDefault(),FLOAT_FORMAT_MASK,values[1])
                    + ";" + String.format(Locale.getDefault(),FLOAT_FORMAT_MASK,values[2]) + "\n";
        return "";
    }

    private void resetIterator(int type){
        iteratorMap.put(type, 0);
    }

    private void resetIterators(){
        resetIterator(Sensor.TYPE_ACCELEROMETER);
        resetIterator(Sensor.TYPE_GYROSCOPE);
        resetIterator(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void incrementIterator(int type){
        int it = iteratorMap.get(type);
        it++;
        iteratorMap.put(type,it);
    }

    private int getIteratorValue(int type){
        return iteratorMap.get(type);
    }

    private boolean isLogOpen(int type){
        return isOpenMap.get(type);
    }

    private boolean areAllLogsClosed(){
        return !isLogOpen(Sensor.TYPE_ACCELEROMETER)
        && !isLogOpen(Sensor.TYPE_GYROSCOPE)
        && !isLogOpen(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void closeLog(int type){
        ARLog.d("[%s]::[log %s closed]", TAG, type);
        isOpenMap.put(type, false);
    }

    private void checkStorageAvailability(){
        boolean available;
        boolean writable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            available = writable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            available = true;
            writable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            available = writable = false;
        }

        ARLog.d("[%s]::[writable: %s, readable: %s]", TAG, writable, available);
    }

    private File createNewLog(Context context, String nameWithoutExtension){
        File file = new File(context.getExternalFilesDir(null), nameWithoutExtension + LOG_EXTENSION);

        int it = 1;

        while (file.exists() && file.canWrite()) {
            file = new File(context.getExternalFilesDir(null), nameWithoutExtension + it + LOG_EXTENSION);
            it++;
        }


        return file;
    }

}
