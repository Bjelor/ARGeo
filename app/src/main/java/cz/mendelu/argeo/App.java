package cz.mendelu.argeo;

import android.app.Application;
import android.location.Location;

/**
 * Created by Bjelis on 11.5.2017.
 */

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();


    public static final Location front = new Location("manual");
    static {
        front.setLatitude(49.2478566d);
        front.setLongitude(16.5743196d);
        front.setAltitude(17.5d);
    }


    public static final Location vut = new Location("manual");
    static {
        vut.setLatitude(49.2483308d);
        vut.setLongitude(16.5725714d);
        vut.setAltitude(17.5d);
    }

    private static App sInstance;

    private static OrientationHelper sOrientationHelper;
    private static LocationHelper sLocationHelper;

    public static App getInstance(){
        return sInstance;
    }

    public static Location getLastLocation(){
        return sLocationHelper != null ? sLocationHelper.getLastLocation() : null;
    }

    public static boolean isRawOrientation( ){
        return sOrientationHelper != null && sOrientationHelper.isRaw();
    }

    public static void setRawOrientation(boolean raw){
        if(sOrientationHelper != null)
            sOrientationHelper.setRaw(raw);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        sOrientationHelper = new OrientationHelper(this);
        sLocationHelper = new LocationHelper(this);
    }

}
