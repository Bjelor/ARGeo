package cz.mendelu.argeo;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import cz.mendelu.argeo.eventbus.LocationMsgEvt;
import cz.mendelu.argeo.util.ARLog;

/**
 * Created by Bjelis on 12.5.2017.
 */

public class LocationHelper {

    private static final String TAG = LocationHelper.class.getSimpleName();

    private Location lastLocation = null;

    public Location getLastLocation(){
        return lastLocation;
    }

    public LocationHelper(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                EventBus.getDefault().post(LocationMsgEvt.generate(lastLocation));
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



        try {
            locationManager.requestLocationUpdates(best, 50, 0, locationListener);
            //this segment causes generation of almost random initial position - not recommended to use
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(best);
            }
        } catch (SecurityException e){
            ARLog.e("[%s]::[SecurityException: %s]", TAG, e.getMessage());
        }
    }
}
