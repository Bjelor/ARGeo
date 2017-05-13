package cz.mendelu.argeo.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import cz.mendelu.argeo.App;
import cz.mendelu.argeo.util.ARLog;

/**
 * A temporary overlay providing virtual data. Will be replaced by something more suitable in the future.
 * @author adamb_000
 * @since 13. 7. 2016
 */
    //TODO: this class appears unnecessary - use textviews for sensor data if needed
    //TODO: LocationManager should probably be located in an Activity
    //FIXME: bad behavior on screen orientation change
public class OverlayView extends View {

    // ========================================================================
    // =====================   C  O  N  S  T  A  N  T  S   ====================
    // ========================================================================

    public static final String TAG = OverlayView.class.getSimpleName();

    final Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ========================================================================
    // ========================   M  E  M  B  E  R  S   =======================
    // ========================================================================

    float[] lastOrientation;

    // ========================================================================
    // =======================    M  E  T  H  O  D  S   =======================
    // ========================================================================

    public OverlayView(Context context) {
        super(context);

        targetPaint.setColor(0xFF00FF00);
        contentPaint.setTextAlign(Paint.Align.CENTER);
        contentPaint.setTextSize(20);
        contentPaint.setColor(Color.RED);

        setBackgroundColor(context.getResources().getColor(android.R.color.transparent));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lastOrientation == null)
            return;

        if (ArDisplayView.getCamera() == null){
            ARLog.e("[%s]::[camera was null]",TAG);
            return;
        }

        float verticalFOV = ArDisplayView.getCamera().getVerticalAngle();

        float horizontalFOV = ArDisplayView.getCamera().getHorizontalAngle();

        // use roll for screen rotation
        canvas.rotate((float)(0.0f- Math.toDegrees(lastOrientation[2])));
        // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
        float dx = (float) ( (canvas.getWidth()/ horizontalFOV) * Math.toDegrees(lastOrientation[0]));
        float dy = (float) ( (canvas.getHeight()/ verticalFOV) * Math.toDegrees(lastOrientation[1])) ;

        // wait to translate the dx so the horizon doesn't get pushed off
        canvas.translate(0.0f, 0.0f-dy);

        // make our line big enough to draw regardless of rotation and translation
        canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, targetPaint);

        // now translate the dx
        canvas.translate(0.0f-dx, 0.0f);

        // draw our point -- we've rotated and translated this to the right spot already
        canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 8.0f, contentPaint);


        canvas.drawText("N", canvas.getWidth()/2, (canvas.getHeight()*15)/32, contentPaint);
        canvas.scale(2.5f, 2.5f);

    }

    public void setLastOrientation(float[] lastOrientation) {
        this.lastOrientation = lastOrientation;
        invalidate();
    }

}