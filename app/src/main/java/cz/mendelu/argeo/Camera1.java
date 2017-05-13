package cz.mendelu.argeo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

import cz.mendelu.argeo.util.ARLog;

/**
 * This object wraps an {@link android.hardware.Camera} object for devices with API lvl lower than 21
 * @author adamb_000
 * @since 20. 7. 2016
 */
public class Camera1 implements CameraWrapper {

    // ========================================================================
    // =====================   C  O  N  S  T  A  N  T  S   ====================
    // ========================================================================

    public static final String TAG = Camera1.class.getSimpleName();

    // ========================================================================
    // ========================   M  E  M  B  E  R  S   =======================
    // ========================================================================

    private android.hardware.Camera mCamera;

    // ========================================================================
    // =======================    M  E  T  H  O  D  S   =======================
    // ========================================================================

    @Override
    public void release() {
        ARLog.d("[%s]::[release()]", TAG);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void open() {
        ARLog.d("[%s]::[open()]", TAG);
        try {
            if (mCamera == null) {
                mCamera = android.hardware.Camera.open();
            } else {
                //TODO: check what this else branch actually does
                release();
                open();
            }
        } catch(RuntimeException e) {
            ARLog.e("[%s]::[getCamera RuntimeException: %s]", TAG, e.getMessage());
        }
    }

    @Override
    public void setOrientation(int degrees) {
        ARLog.d("[%s]::[setOrientation()]", TAG);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK, info);

        //cam
        mCamera.setDisplayOrientation((info.orientation - degrees + 360) % 360);
    }

    @Override
    public void setPreviewDisplay(SurfaceHolder holder) {
        ARLog.d("[%s]::[setPreviewDisplay()]", TAG);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            ARLog.e("[%s]::[setPreviewDisplay IOException: %s]", TAG, e.getMessage());
        }
    }

    @Override
    public void setPreviewTexture(SurfaceTexture holder) {
        ARLog.d("[%s]::[setPreviewDisplay()]", TAG);
        try {
            mCamera.setPreviewTexture(holder);
        } catch (IOException e) {
            ARLog.e("[%s]::[setPreviewDisplay IOException: %s]", TAG, e.getMessage());
        }
    }

    @Override
    public void setPreviewDisplaySize(int width, int height) {

        ARLog.d("[%s]::[setPreviewDisplay()]", TAG);

        android.hardware.Camera.Parameters params = mCamera.getParameters();
        List<android.hardware.Camera.Size> prevSizes = params.getSupportedPreviewSizes();
        for (android.hardware.Camera.Size s : prevSizes)
        {
            if((s.height <= height) && (s.width <= width))
            {
                params.setPreviewSize(s.width, s.height);
                break;
            }
        }

        mCamera.setParameters(params);
    }

    @Override
    public void startPreview() {
        ARLog.d("[%s]::[startPreview()]", TAG);
        mCamera.startPreview();
    }

    @Override
    public float getVerticalAngle() {
        return mCamera.getParameters().getVerticalViewAngle();
    }

    @Override
    public float getHorizontalAngle() {
        return mCamera.getParameters().getHorizontalViewAngle();
    }

    @Override
    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
