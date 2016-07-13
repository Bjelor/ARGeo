package cz.mendelu.argeo.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

import cz.mendelu.argeo.util.ARLog;

/**
 * @author adamb_000
 * @since 13. 7. 2016
 */
public class ArDisplayView extends SurfaceView {

    public static final String TAG = ArDisplayView.class.getSimpleName();

    //FIXME: possibly move Camera altogether to Activity or App
    static Camera mCamera;
    SurfaceHolder mHolder;
    Context mContext;

    public ArDisplayView(Context context) {
        super(context);

        mContext = context;
        mHolder = getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                releaseCamera();
                mCamera = Camera.open();
            } catch (RuntimeException e) {
                ARLog.e("[%s]::[surfaceCreated exception: %s]", TAG, e.getMessage());
                return;
            }

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int rotation = wm.getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }
            //cam
            mCamera.setDisplayOrientation((info.orientation - degrees + 360) % 360);

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                ARLog.e("[%s]::[surfaceCreated exception: %s]", TAG, e.getMessage());
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            if(mCamera == null) {
                return;
            }

            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
            for (Camera.Size s : prevSizes)
            {
                if((s.height <= height) && (s.width <= width))
                {
                    params.setPreviewSize(s.width, s.height);
                    break;
                }
            }

            mCamera.setParameters(params);
            mCamera.startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if(mCamera == null) {
                return;
            }

            //cam
            releaseCamera();
        }
    };

    public static Camera getCamera(){
        return mCamera;
    }
}