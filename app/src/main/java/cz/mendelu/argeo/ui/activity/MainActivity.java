package cz.mendelu.argeo.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.mendelu.argeo.App;
import cz.mendelu.argeo.R;
import cz.mendelu.argeo.SensorLog;
import cz.mendelu.argeo.eventbus.LocationMsgEvt;
import cz.mendelu.argeo.eventbus.OrientationMsgEvt;
import cz.mendelu.argeo.eventbus.SensorMsgEvt;
import cz.mendelu.argeo.ui.ScalpelFrameLayout;
import cz.mendelu.argeo.ui.view.ArDisplayView;
import cz.mendelu.argeo.ui.view.OverlayView;
import cz.mendelu.argeo.util.ARLog;

public class MainActivity extends AppCompatActivity{

    // ========================================================================
    // =====================   C  O  N  S  T  A  N  T  S   ====================
    // ========================================================================
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    // ========================================================================
    // ========================   M  E  M  B  E  R  S   =======================
    // ========================================================================


    FrameLayout mArViewPane;
    OverlayView mOverlayView;

    ArDisplayView mArDisplay;

    RelativeLayout mLoadingContainer;

    LinearLayout mPositionContainer;

    TextView mTextLat;
    TextView mTextLon;
    TextView mTextBea;

    private boolean mShouldStartLogging = false;

    private List<ScalpelFrameLayout> mPOIList = new ArrayList<>();

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

        mLoadingContainer = (RelativeLayout) findViewById(R.id.loading_container);

        mPositionContainer = (LinearLayout) findViewById(R.id.location_container);

        setLocationViews(App.getLastLocation());

        if (arePermissionsGranted()){
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
//        mMapBoxView.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if(!mPOIList.isEmpty()){
            for (ScalpelFrameLayout sfl : mPOIList) {
                sfl.setLastOrientation(event.getValues());
            }
        }

        if(mOverlayView != null){
            mOverlayView.setLastOrientation(event.getValues());
        }

        if(mTextBea != null){
            mTextBea.setText(String.format(Locale.getDefault(),"azimuth: %.2f",event.getValues()[0]));
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
        if(!mPOIList.isEmpty()){
            for (ScalpelFrameLayout sfl : mPOIList) {
                sfl.calculateDistance();
            }
        }
    }

    private void setLocationViews(Location location){
        if(location == null) {
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
                mTextLat.setText(String.format(Locale.getDefault(),"lat: %.4f",location.getLatitude()));
                mTextLon.setText(String.format(Locale.getDefault(),"lon: %.4f",location.getLongitude()));
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
    private void initArViews(){

        mArViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);
        mArDisplay = new ArDisplayView(MainActivity.this);
        mArViewPane.addView(mArDisplay);
        mOverlayView = new OverlayView(getApplicationContext());
        mArViewPane.addView(mOverlayView);

        ScalpelFrameLayout sfl = new ScalpelFrameLayout(this,
                App.front, "Front", "-");
        sfl.calculateDistance();
        mPOIList.add(sfl);

        ScalpelFrameLayout sfl2 = new ScalpelFrameLayout(this,
                App.vut, "Left", "-");
        sfl2.calculateDistance();
        mPOIList.add(sfl2);

        mArViewPane.addView(sfl);
        mArViewPane.addView(sfl2);
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

    private void requestPermissions(){

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

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    public static void call(Activity activity)
    {
        // Creating an intent with the current activity and the activity we wish to start
        Intent myIntent = new Intent(activity, MainActivity.class);
        activity.startActivity(myIntent);
    }

    public static String Testcall()
    {
        return "android string returned";
    }



}
