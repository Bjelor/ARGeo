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

}
