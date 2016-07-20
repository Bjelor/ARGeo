package cz.mendelu.argeo;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import cz.mendelu.argeo.util.ARLog;

/**
 * @author adamb_000
 * @since 20. 7. 2016
 */

@Singleton
@TargetApi(21)
public class Camera2 implements Camera {

    public static final String TAG = Camera2.class.getSimpleName();

    private CameraDevice mCameraDevice;
    private CameraManager mCameraManager;
    private CameraCharacteristics mCameraCharacteristics;
    private CameraCaptureSession mCameraCaptureSession;

    private boolean shouldStartPreview = false;
    private boolean shouldStartCaptureSession = false;
    private boolean shouldSetOrientation = false;
    private boolean shouldSetDisplaySize = false;

    private boolean isCaptureSessionRunning = false;

    private SurfaceHolder mHolder = null;
    private int mWidth = -1;
    private int mHeight = -1;

    private Context mContext;

    public Camera2(Context context){
        this.mContext = context;
    }

    @Override
    public void release() {
        ARLog.d("[%s]::[release()]", TAG);
        try {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

            if (mCameraManager != null) {
                mCameraManager = null;
            }
        } catch (RuntimeException e) {
            ARLog.e("[%s]::[releaseCamera RuntimeException: %s]", TAG, e.getMessage());
        }
    }

    @Override
    public void open() {
        ARLog.d("[%s]::[open()]", TAG);

        try {
            if(mCameraManager == null) {
                mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            }

            if(mCameraDevice == null) {
                try {
                    mCameraManager.openCamera(mCameraManager.getCameraIdList()[0], mCameraStateCallback, null);
                    mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraManager.getCameraIdList()[0]);
                } catch (SecurityException e) {
                    ARLog.e("[%s]::[open()]::[SecurityException: %s]", TAG, e. getMessage());
                } catch (CameraAccessException e) {
                    ARLog.e("[%s]::[open()]::[CameraAccessException: %s]", TAG, e. getMessage());
                }
            } else {
                //TODO: check what this else branch actually does
                release();
                open();
            }
        } catch(RuntimeException e) {
            ARLog.e("[%s]::[open()]::[RuntimeException: %s]", TAG, e.getMessage());
        }
    }

    @Override
    public void setOrientation(int orientation) {

    }

    @Override
    public void setPreviewDisplay(SurfaceHolder holder) {
        ARLog.d("[%s]::[setPreviewDisplay()]", TAG);

        mHolder = holder;

        //TODO: find out what happens if this is true
        if(isCaptureSessionRunning){
            startPreview();
        }
    }

    @Override
    public void setPreviewDisplaySize(int width, int height) {
        ARLog.d("[%s]::[setPreviewDisplaySize()]", TAG);

        mWidth = width;
        mHeight = height;

        if(mCameraManager != null && mHolder != null) {
            try {
//                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraManager.getCameraIdList()[0]);
                Size[] outputSizes = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceHolder.class);
                for (Size s : outputSizes) {
                    if ((s.getHeight() <= height) && (s.getWidth() <= width)) {
                        mHolder.setFixedSize(s.getWidth(), s.getHeight());
                        break;
                    }
                }
            } catch (NullPointerException e) {
                ARLog.e("[%s]::[open()]::[NullPointerException: %s]", TAG, e. getMessage());
            }
        }
    }



    @Override
    public void startPreview() {
        ARLog.d("[%s]::[startPreview()]", TAG);
        if(mCameraDevice != null) {
            ARLog.d("[%s]::[startPreview()]::[mCameraDevice set]", TAG);

            shouldStartPreview = false;

            List<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(mHolder.getSurface());

            try {
                mCameraDevice.createCaptureSession(surfaceList, mCameraCaptureSessionStateCallback, null);
                startCaptureSession();
            } catch (CameraAccessException e) {
                ARLog.e("[%s]::[startPreview()]::[CameraAccessException: %s]", TAG, e.getMessage());
            }
        } else {
            ARLog.e("[%s]::[startPreview()]::[mCameraDevice not set]", TAG);
            shouldStartPreview = true;
        }
    }

    private void startCaptureSession() {
        ARLog.d("[%s]::[startCaptureSession()]", TAG);
        if(mCameraDevice == null || mCameraCaptureSession == null) {
            ARLog.e("[%s]::[startCaptureSession()]::[not ready]", TAG);
            shouldStartCaptureSession = true;
        } else {
            ARLog.d("[%s]::[startCaptureSession()]::[all set]", TAG);
            shouldStartCaptureSession = false;
            try {
                CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(mHolder.getSurface());
                CaptureRequest request = builder.build();
                mCameraCaptureSession.setRepeatingRequest(request, mCaptureCallback, null);
            } catch (CameraAccessException e) {
                ARLog.e("[%s]::[startCaptureSession()]::[CameraAccessException: %s]", TAG, e.getMessage());
            }
        }
    }

    @Override
    public float getVerticalAngle() {
//        ARLog.d("[%s]::[getVerticalAngle()]", TAG);
        try {
//            CameraCharacteristics camchars = mCameraManager.getCameraCharacteristics(mCameraManager.getCameraIdList()[0]);
            return mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (NullPointerException e) {
            ARLog.e("[%s]::[getVerticalAngle()]::[NullPointerException: %s]", TAG, e.getMessage());
        }
        return 49;
    }

    @Override
    public float getHorizontalAngle() {
//        ARLog.d("[%s]::[getHorizontalAngle()]", TAG);
        try {
            return mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (NullPointerException e) {
            ARLog.e("[%s]::[getVerticalAngle()]::[NullPointerException: %s]", TAG, e.getMessage());
        }
        return 60;
    }


    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            if(shouldStartPreview){
                startPreview();
            }
            ARLog.d("[%s]::[CameraDevice.StateCallback]::[onOpened]", TAG);
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            ARLog.d("[%s]::[CameraDevice.StateCallback]::[onDisconnected]", TAG);
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            ARLog.e("[%s]::[CameraDevice.StateCallback]::[onError]", TAG);
        }
    };

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            ARLog.d("[%s]::[CameraCaptureSession.StateCallback]::[onConfigured]", TAG);
            mCameraCaptureSession = cameraCaptureSession;

            if(shouldStartCaptureSession){
                Camera2.this.startCaptureSession();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            ARLog.e("[%s]::[CameraCaptureSession.StateCallback]::[onConfigureFailed]", TAG);
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
//            ARLog.d("[%s]::[CameraCaptureSession.CaptureCallback]::[onCaptureStarted]", TAG);
            isCaptureSessionRunning = true;
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//            ARLog.d("[%s]::[CameraCaptureSession.CaptureCallback]::[onCaptureCompleted]", TAG);
            isCaptureSessionRunning = false;
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            ARLog.e("[%s]::[CameraCaptureSession.CaptureCallback]::[onCaptureFailed]", TAG);
            isCaptureSessionRunning = false;
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            ARLog.d("[%s]::[CameraCaptureSession.CaptureCallback]::[onCaptureSequenceCompleted]", TAG);
            isCaptureSessionRunning = false;
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            ARLog.e("[%s]::[CameraCaptureSession.CaptureCallback]::[onCaptureSequenceAborted]", TAG);
            isCaptureSessionRunning = false;
        }

        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            ARLog.e("[%s]::[CameraCaptureSession.CaptureCallback]::[onCaptureBufferLost]", TAG);
            isCaptureSessionRunning = false;
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            ARLog.e("[%s]::[CameraCaptureSession.CaptureCallback]::[onCaptureProgressed]", TAG);
            isCaptureSessionRunning = false;
        }
    };
}
