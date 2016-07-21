package cz.mendelu.argeo.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import javax.inject.Singleton;

import cz.mendelu.argeo.CameraWrapper;
import cz.mendelu.argeo.Camera1;
import cz.mendelu.argeo.Camera2;
import dagger.Provides;

/**
 * A {@link SurfaceView} for viewing real-world camera feed. Is currently overlayed by {@link OverlayView}
 * @author adamb_000
 * @since 13. 7. 2016
 */
public class ArDisplayView extends SurfaceView {

    // ========================================================================
    // =====================   C  O  N  S  T  A  N  T  S   ====================
    // ========================================================================

    public static final String TAG = ArDisplayView.class.getSimpleName();

    // ========================================================================
    // ========================   M  E  M  B  E  R  S   =======================
    // ========================================================================

    //FIXME: possibly move Camera altogether to Activity or App
    static CameraWrapper mCamera;
    SurfaceHolder mHolder;
    Context mContext;

    // ========================================================================
    // =======================    M  E  T  H  O  D  S   =======================
    // ========================================================================

    public ArDisplayView(Context context) {
        super(context);

        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(mSurfaceHolderCallback);
    }

    public ArDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ArDisplayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Provides
    @Singleton
    public static CameraWrapper getCamera(){
        return mCamera;
    }

    // ========================================================================
    // =====================   C  A  L  L  B  A  C  K  S   ====================
    // ========================================================================

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {

        public void surfaceCreated(SurfaceHolder holder) {

            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int rotation = wm.getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            if(Build.VERSION.SDK_INT >= 21){
                mCamera = new Camera2(mContext);
            } else {
                mCamera = new Camera1();
            }

            mCamera.open();

            mCamera.setOrientation(degrees);

            mCamera.setPreviewDisplay(mHolder);

            mCamera.startPreview();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            if(mCamera == null) {
                return;
            }
            mCamera.setPreviewDisplaySize(width, height);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if(mCamera == null) {
                return;
            }

            //cam
            mCamera.release();
        }
    };

}