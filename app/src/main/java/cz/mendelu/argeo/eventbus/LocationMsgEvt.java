package cz.mendelu.argeo.eventbus;

import android.location.Location;

/**
 * Created by Bjelis on 11.5.2017.
 */

public class LocationMsgEvt {
    private Location location;

    public LocationMsgEvt(Location location) {
        this.location = location;
    }

    public static LocationMsgEvt generate(Location loc){
        return new LocationMsgEvt(loc);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
