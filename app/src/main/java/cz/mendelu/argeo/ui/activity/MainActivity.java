package cz.mendelu.argeo.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.mendelu.argeo.App;
import cz.mendelu.argeo.KMLParser;
import cz.mendelu.argeo.Placemark;
import cz.mendelu.argeo.R;
import cz.mendelu.argeo.SensorLog;
import cz.mendelu.argeo.eventbus.LocationMsgEvt;
import cz.mendelu.argeo.eventbus.OrientationMsgEvt;
import cz.mendelu.argeo.eventbus.SensorMsgEvt;
import cz.mendelu.argeo.ui.view.FloatingFrameLayout;
import cz.mendelu.argeo.ui.view.ArDisplayView;
import cz.mendelu.argeo.ui.view.OverlayView;
import cz.mendelu.argeo.util.ARLog;

public class MainActivity extends AppCompatActivity {

    // ========================================================================
    // =====================   C  O  N  S  T  A  N  T  S   ====================
    // ========================================================================
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private static final int REQUEST_KML_FILE = 2;


    // ========================================================================
    // ========================   M  E  M  B  E  R  S   =======================
    // ========================================================================

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationDrawer;


    FrameLayout mArViewPane;
    OverlayView mOverlayView;

    ArDisplayView mArDisplay;

    RelativeLayout mLoadingContainer;
    RelativeLayout mPositionContainer;

    TextView mTextLat;
    TextView mTextLon;
    TextView mTextBea;

    ImageButton mMenuBtn;

    private boolean mShouldStartLogging = false;

    private List<FloatingFrameLayout> mPOIList = new ArrayList<>();

    // ========================================================================
    // =======================    H  A  N  D  L  E  R   =======================
    // ========================================================================

    // ========================================================================
    // =======================    M  E  T  H  O  D  S   =======================
    // ========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ARLog.d("[%s]::[onCreate]", TAG);

        setContentView(R.layout.activity_main);

        mTextBea = (TextView) findViewById(R.id.bearing);
        mTextLat = (TextView) findViewById(R.id.latitude);
        mTextLon = (TextView) findViewById(R.id.longitude);

        initDrawer();

        mLoadingContainer = (RelativeLayout) findViewById(R.id.loading_container);

        mPositionContainer = (RelativeLayout) findViewById(R.id.location_container);

        setLocationViews(App.getLastLocation());

        if (arePermissionsGranted()) {
            initArViews();
        } else {
            requestPermissions();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_KML_FILE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                ARLog.d("[%s]::[uri loaded: %s]", TAG, uri.toString());

                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    List<Placemark> placemarks = KMLParser.parseFile(inputStream);
                    loadPlacemarks(placemarks);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mArViewPane != null && mArDisplay != null /*&& mArViewPane.indexOfChild(mArDisplay) < 0*/) {
            mArViewPane.setVisibility(View.VISIBLE);
            mArDisplay.setVisibility(View.VISIBLE);
//            mArViewPane.addView(mArDisplay);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mArViewPane != null && mArDisplay != null) {
            mArViewPane.setVisibility(View.GONE);
            mArDisplay.setVisibility(View.GONE);
//            mArViewPane.removeView(mArDisplay);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // ------------------------------------------------------------------------
    // ---  /\  E N D : L I F E C Y C L E S --------------------------  ||  ---
    // --- /||\ ------------------------------------------------------ \||/ ---
    // ---  ||  ------------------------------ E V E N T   A C T I O N  \/  ---
    // ------------------------------------------------------------------------

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final OrientationMsgEvt event) {
        if (!mPOIList.isEmpty()) {
            for (FloatingFrameLayout sfl : mPOIList) {
                sfl.setLastOrientation(event.getValues());
            }
        }

        if (mOverlayView != null) {
            mOverlayView.setLastOrientation(event.getValues());
        }

        if (mTextBea != null) {
            mTextBea.setText(String.format(Locale.getDefault(), "azimuth: %.2f", event.getCurrentBearing()));
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final SensorMsgEvt event) {
        if (event.getType() == Sensor.TYPE_GYROSCOPE) {
            if (mShouldStartLogging) {
                SensorLog.getInstance().performLog(event.getType(), event.getValues(), this);
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final LocationMsgEvt event) {
        setLocationViews(event.getLocation());
        if (!mPOIList.isEmpty()) {
            for (FloatingFrameLayout sfl : mPOIList) {
                sfl.calculateDistance();
            }
        }
    }

    private void setLocationViews(Location location) {
        if (location == null) {
            if (mLoadingContainer != null) {
                mLoadingContainer.setVisibility(View.VISIBLE);
            }
            if (mPositionContainer != null) {
                mPositionContainer.setVisibility(View.GONE);
            }
        } else {
            if (mLoadingContainer != null) {
                mLoadingContainer.setVisibility(View.GONE);
            }
            if (mTextLat != null && mTextLon != null && mTextBea != null) {
                mPositionContainer.setVisibility(View.VISIBLE);
                mTextLat.setText(String.format(Locale.getDefault(), "lat: %.4f", location.getLatitude()));
                mTextLon.setText(String.format(Locale.getDefault(), "lon: %.4f", location.getLongitude()));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ARLog.e("[%s]::[onRequestPermissionsResult]::[requestCode %s]::[cancelled]", TAG, requestCode);

                } else {

                    ARLog.e("[%s]::[onRequestPermissionsResult]::[requestCode %s]::[granted]", TAG, requestCode);

                    initArViews();
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // ---  /\  E N D : E V E N T    A C T I O N  --------------------  ||  ---
    // --- /||\ ------------------------------------------------------ \||/ ---
    // ---  ||  ------------------------ P U B L I C   F U N C T I O N  \/  ---
    // ------------------------------------------------------------------------


    // ------------------------------------------------------------------------
    // ---  /\  E N D : P U B L I C   F U N C T I O N  ---------------  ||  ---
    // --- /||\ ------------------------------------------------------ \||/ ---
    // ---  ||  ---------------------------- H E L P   F U N C T I O N  \/  ---
    // ------------------------------------------------------------------------

    private void initDrawer() {
        mMenuBtn = (ImageButton) findViewById(R.id.menu_btn);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawer = (NavigationView) findViewById(R.id.nav_drawer);

        String[] menuItems = {"Load from file", "Load from URL", "View distance", "Noise filter", "Sensor calibration"};

        mMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });

        // Set the adapter for the list view
        mNavigationDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.menu_item_load_kml){
                    // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                    // browser.
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                    // Filter to only show results that can be "opened", such as a
                    // file (as opposed to a list of contacts or timezones)
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    // Filter to show only images, using the image MIME data type.
                    // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                    // To search for all documents available via installed storage providers,
                    // it would be "*/*".
                    intent.setType("application/vnd.google-earth.kml+xml");

                    startActivityForResult(intent, REQUEST_KML_FILE);

                } else if(id == R.id.menu_item_view_distance) {

                } else if(id == R.id.menu_item_sensor_filter) {

                }
                mDrawerLayout.closeDrawer(Gravity.LEFT);
                return true;

            }
        });
    }

    private void initArViews() {

        mArViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);
        mArDisplay = new ArDisplayView(MainActivity.this);
        mArViewPane.addView(mArDisplay);
        mOverlayView = new OverlayView(getApplicationContext());
        mArViewPane.addView(mOverlayView);
    }

    private void loadPlacemarks(List<Placemark> placemarks) {
        if(mArViewPane != null)
            for (Placemark placemark : placemarks) {
                FloatingFrameLayout sfl = new FloatingFrameLayout(this);
                sfl.loadPlacemark(placemark);
                sfl.calculateDistance();
                mPOIList.add(sfl);

                mArViewPane.addView(sfl);
            }
    }

    private boolean arePermissionsGranted() {

        boolean camera;
        boolean coarseLocation;
        boolean fineLocation;

        camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return (camera && coarseLocation && fineLocation);
    }

    private void requestPermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            ARLog.e("[%s]::[onCreate]::[shouldShowRequestPermissionRationale]", TAG);

            //show request rationale here

        } else {

            ARLog.v("[%s]::[onCreate]::[permissions requested]", TAG);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }
}