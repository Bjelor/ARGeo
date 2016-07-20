package cz.mendelu.argeo;

import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import javax.inject.Singleton;

/**
 * @author adamb_000
 * @since 19. 7. 2016
 */
@Singleton
public class CameraProvider {

    public static final String TAG = CameraProvider.class.getSimpleName();

    private static Camera sCamera;
    private static CameraDevice sCameraDevice;
    private static CameraManager sCameraManager;

//
//    public static void releaseCamera(){
//
//        try {
//            if (sCameraDevice != null && Build.VERSION.SDK_INT >= 21) {
//                sCameraDevice.close();
//                sCameraDevice = null;
//            } else if (sCamera != null) {
//                sCamera.stopPreview();
//                sCamera.release();
//                sCamera = null;
//            }
//        } catch (RuntimeException e) {
//            ARLog.e("[%s]::[releaseCamera RuntimeException: %s]", TAG, e.getMessage());
//        }
//    }
//
//    @Provides
//    @Singleton
//    public static Camera getCamera(){
//        ARLog.d("[%s]::[getCamera()]", TAG);
//        try {
//            if (sCamera == null) {
//                sCamera = Camera.open();
//            }
//        } catch(RuntimeException e) {
//            ARLog.e("[%s]::[getCamera RuntimeException: %s]", TAG, e.getMessage());
//            return null;
//        }
//        return sCamera;
//    }
//
//    //TODO: find a way to handle camera callbacks
//    @TargetApi(21)
//    public static CameraDevice getCameraDevice(Context context){
//
//        ARLog.d("[%s]::[getCameraDevice() API 21]", TAG);
//
//        try {
//            if(sCameraManager == null) {
//                sCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//            }
//
//            if(sCameraDevice == null) {
//                try {
//                    sCameraManager.openCamera(sCameraManager.getCameraIdList()[0], getStateCallback(), null);
//                } catch (SecurityException e) {
//                    ARLog.e("[%s]::[API:21 getCameraDevice()]::[SecurityException: %s]", TAG, e. getMessage());
//                } catch (CameraAccessException e) {
//                    ARLog.e("[%s]::[API:21 getCameraDevice()]::[CameraAccessException: %s]", TAG, e. getMessage());
//                }
//            }
//        } catch(RuntimeException e) {
//            ARLog.e("[%s]::[getCameraDevice RuntimeException: %s]", TAG, e.getMessage());
//            return null;
//        }
//
//        return sCameraDevice;
//    }
//
//    @TargetApi(21)
//    private static CameraDevice.StateCallback getStateCallback() {
//            return new CameraDevice.StateCallback() {
//            @Override
//            public void onOpened(CameraDevice cameraDevice) {
//                sCameraDevice = cameraDevice;
//                ARLog.d("[%s]::[CameraDevice.StateCallback]::[onOpened]", TAG);
//            }
//
//            @Override
//            public void onDisconnected(CameraDevice cameraDevice) {
//                ARLog.d("[%s]::[CameraDevice.StateCallback]::[onDisconnected]", TAG);
//            }
//
//            @Override
//            public void onError(CameraDevice cameraDevice, int i) {
//                ARLog.d("[%s]::[CameraDevice.StateCallback]::[onError]", TAG);
//            }
//        };
//    }
}
