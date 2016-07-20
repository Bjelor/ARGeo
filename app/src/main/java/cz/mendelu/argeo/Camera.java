package cz.mendelu.argeo;

import android.view.SurfaceHolder;

/**
 * @author adamb_000
 * @since 20. 7. 2016
 */
public interface Camera {

    void release();

    void open();

    void setOrientation(int degrees);

    void setPreviewDisplay(SurfaceHolder holder);

    void setPreviewDisplaySize(int width, int height);

    void startPreview();

    float getVerticalAngle();

    float getHorizontalAngle();

}
