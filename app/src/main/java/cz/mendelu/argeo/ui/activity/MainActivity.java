package cz.mendelu.argeo.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.StartupConfiguration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.mendelu.argeo.App;
import cz.mendelu.argeo.R;
import cz.mendelu.argeo.util.ARLog;

public class MainActivity extends AppCompatActivity {

    // ========================================================================
    // =====================   C  O  N  S  T  A  N  T  S   ====================
    // ========================================================================
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_CAMERA = 0;

    // ========================================================================
    // ========================   M  E  M  B  E  R  S   =======================
    // ========================================================================
    @BindView(R.id.ar_view_pane)
    FrameLayout mArViewPane;

    @BindView(R.id.arview)
    ArchitectView mArView;

    Unbinder mUnbinder;

    // ========================================================================
    // =======================    M  E  T  H  O  D  S   =======================
    // ========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ARLog.v("[%s]::[onCreate]", TAG);

        setContentView(R.layout.activity_main);

        mUnbinder = ButterKnife.bind(this);

        if (arePermissionsGranted()){
            initArViews();
        } else {
            requestPermissions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    // ------------------------------------------------------------------------
    // ---  /\  E N D : L I F E C Y C L E S --------------------------  ||  ---
    // --- /||\ ------------------------------------------------------ \||/ ---
    // ---  ||  ------------------------------ E V E N T   A C T I O N  \/  ---
    // ------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

    //empty

    // ------------------------------------------------------------------------
    // ---  /\  E N D : P U B L I C   F U N C T I O N  ---------------  ||  ---
    // --- /||\ ------------------------------------------------------ \||/ ---
    // ---  ||  ---------------------------- H E L P   F U N C T I O N  \/  ---
    // ------------------------------------------------------------------------
    private void initArViews(){

        final StartupConfiguration config = new StartupConfiguration(App.WIKITUDE_KEY);
        mArView.onCreate(config);

//        ArDisplayView arDisplay = new ArDisplayView(this);
//        mArViewPane.addView(arDisplay);
//
//        OverlayView arContent = new OverlayView(getApplicationContext());
//        mArViewPane.addView(arContent);
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
                    Manifest.permission.CAMERA)) {

            ARLog.e("[%s]::[onCreate]::[shouldShowRequestPermissionRationale]", TAG);

            //show request rationale here

        } else {

            ARLog.v("[%s]::[onCreate]::[permissions requested]", TAG);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
    }
}
