package cz.mendelu.argeo;

import android.view.SurfaceHolder;

/**
 * An interface to wrap both {@link android.hardware.Camera} and {@link android.hardware.camera2}
 * objects to support all SDK versions
 * @author adamb_000
 * @since 20. 7. 2016
 */
public interface CameraWrapper {

    /**
     * equivalent to {@link android.hardware.Camera#release()}
     */
    void release();

    /**
     * equivalent to {@link android.hardware.Camera#open()}
     */
    void open();

    /**
     * equivalent to {@link android.hardware.Camera#setDisplayOrientation(int)} with adjusted input param.
     * Possibly to be deleted.
     */
    void setOrientation(int degrees);

    /**
     * equivalent to {@link android.hardware.Camera#setPreviewDisplay(SurfaceHolder)}
     */
    void setPreviewDisplay(SurfaceHolder holder);

    /**
     * equivalent to {@link android.hardware.Camera.Parameters#setPreviewSize(int, int)}
     * and consecutively applying params via {@link android.hardware.Camera#setParameters(android.hardware.Camera.Parameters)}
     */
    void setPreviewDisplaySize(int width, int height);

    /**
     * equivalent to {@link android.hardware.Camera#startPreview()}
     */
    void startPreview();

    /**
     * equivalent to {@link android.hardware.Camera.Parameters#getVerticalViewAngle()}
     */
    float getVerticalAngle();

    /**
     * equivalent to {@link android.hardware.Camera.Parameters#getHorizontalViewAngle()}
     */
    float getHorizontalAngle();

}
