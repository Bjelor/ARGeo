package cz.mendelu.argeo.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Locale;

import cz.mendelu.argeo.App;
import cz.mendelu.argeo.Placemark;
import cz.mendelu.argeo.ui.fragment.POIFragment;
import cz.mendelu.argeo.util.ARLog;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * Renders your view hierarchy as an interactive 3D visualization of layers.
 * <p>
 * Interactions supported:
 * <ul>
 * <li>Single touch: controls the rotation of the model.</li>
 * <li>Two finger vertical pinch: Adjust zoom.</li>
 * <li>Two finger horizontal pinch: Adjust layer spacing.</li>
 * </ul>
 */
public class FloatingFrameLayout extends FrameLayout{

    private static final String TAG = FloatingFrameLayout.class.getSimpleName();

    private static final int TRACKING_UNKNOWN = 0;
    private static final int TRACKING_VERTICALLY = 1;
    private static final int TRACKING_HORIZONTALLY = -1;
    private static final int ROTATION_MAX = 60;
    private static final int ROTATION_MIN = -ROTATION_MAX;
    private static final int ROTATION_DEFAULT_X = -10;
    private static final int ROTATION_DEFAULT_Y = 15;
    private static final float ZOOM_DEFAULT = 0.6f;
    private static final float ZOOM_MIN = 0.33f;
    private static final float ZOOM_MAX = 2f;
    private static final int SPACING_DEFAULT = 25;
    private static final int SPACING_MIN = 10;
    private static final int SPACING_MAX = 100;
    private static final int CHROME_COLOR = 0xFF888888;
    private static final int CHROME_SHADOW_COLOR = 0xFF000000;
    private static final int TEXT_OFFSET_DP = 2;
    private static final int TEXT_SIZE_DP = 10;
    private static final int CHILD_COUNT_ESTIMATION = 25;
    private static final boolean DEBUG = false;

    private static void log(String message, Object... args) {
        Log.d("Scalpel", String.format(message, args));
    }

    private static class LayeredView {
        View view;
        int layer;

        void set(View view, int layer) {
            this.view = view;
            this.layer = layer;
        }

        void clear() {
            view = null;
            layer = -1;
        }
    }

    private final Rect viewBoundsRect = new Rect();
    private final Paint viewBorderPaint = new Paint(ANTI_ALIAS_FLAG);
    private final Camera camera = new Camera();
    private final Matrix matrix = new Matrix();
    private final int[] location = new int[2];
    private final BitSet visibilities = new BitSet(CHILD_COUNT_ESTIMATION);
    private final SparseArray<String> idNames = new SparseArray<>();
    private final Deque<LayeredView> layeredViewQueue = new ArrayDeque<>();
    private final Pool<LayeredView> layeredViewPool = new Pool<LayeredView>(CHILD_COUNT_ESTIMATION) {
        @Override
        protected LayeredView newObject() {
            return new LayeredView();
        }
    };

    private final Resources res;

    private boolean enabled = true;
    private boolean drawViews = true;

    private int pointerOne = INVALID_POINTER_ID;
    private float lastOneX;
    private float lastOneY;
    private int pointerTwo = INVALID_POINTER_ID;
    private float lastTwoX;
    private float lastTwoY;
    private int multiTouchTracking = TRACKING_UNKNOWN;

    private Location target = null;

    private Context context;

    private boolean shouldZoom = false;

    float[] lastOrientation;

    private POIFragment poiFragment;

    public FloatingFrameLayout(Context context) {
        this(context, null);
    }

    public FloatingFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.res = context.getResources();

        this.context = context;



        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        setLayoutParams(lp);

        setBackgroundResource(android.R.color.transparent);
    }


    public void loadPlacemark(Placemark placemark){

        if(placemark == null)
            return;

        this.target = placemark.generateLocation();

        String title = placemark.getTitle();
        String description = placemark.getDescription();
        String imageUrl = placemark.getIconUrl();

        if(poiFragment == null) {
            this.poiFragment = POIFragment.newInstance(title, description,imageUrl);
        } else {
            this.poiFragment.setTitle(title);
            this.poiFragment.setDescription(description);
        }

        int id = getId();

        if(id == NO_ID){
            id = generateViewId();
            setId(id);
        }

        if (context instanceof Activity && !poiFragment.isAdded())
            ((Activity) context).getFragmentManager().beginTransaction().add(id, poiFragment).commit();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enabled || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@SuppressWarnings("NullableProblems") MotionEvent event) {
        if (!enabled) {
            return super.onTouchEvent(event);
        }

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                int index = (action == ACTION_DOWN) ? 0 : event.getActionIndex();
                if (pointerOne == INVALID_POINTER_ID) {
                    pointerOne = event.getPointerId(index);
                    lastOneX = event.getX(index);
                    lastOneY = event.getY(index);
                    if (DEBUG)
                        log("Got pointer 1!  id: %s  x: %s  y: %s", pointerOne, lastOneY, lastOneY);
                } else if (pointerTwo == INVALID_POINTER_ID) {
                    pointerTwo = event.getPointerId(index);
                    lastTwoX = event.getX(index);
                    lastTwoY = event.getY(index);
                    if (DEBUG)
                        log("Got pointer 2!  id: %s  x: %s  y: %s", pointerTwo, lastTwoY, lastTwoY);
                } else {
                    if (DEBUG)
                        log("Ignoring additional pointer.  id: %s", event.getPointerId(index));
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                int index = (action != ACTION_POINTER_UP) ? 0 : event.getActionIndex();
                int pointerId = event.getPointerId(index);
                if (pointerOne == pointerId) {
                    // Shift pointer two (real or invalid) up to pointer one.
                    pointerOne = pointerTwo;
                    lastOneX = lastTwoX;
                    lastOneY = lastTwoY;
                    if (DEBUG) log("Promoting pointer 2 (%s) to pointer 1.", pointerTwo);
                    // Clear pointer two and tracking.
                    pointerTwo = INVALID_POINTER_ID;
                    multiTouchTracking = TRACKING_UNKNOWN;
                } else if (pointerTwo == pointerId) {
                    if (DEBUG) log("Lost pointer 2 (%s).", pointerTwo);
                    pointerTwo = INVALID_POINTER_ID;
                    multiTouchTracking = TRACKING_UNKNOWN;
                }
                break;
            }
        }

        return true;
    }

    public void setLastOrientation(float[] values) {
        lastOrientation = values;
        invalidate();
    }

    public void setTargetLocation(double lat, double lon, double alt) {
        target = new Location("manual");
        target.setLatitude(lat);
        target.setLongitude(lon);
        target.setAltitude(alt);
    }

    public void setTargetLocation(Location location) {
        target = location;
    }

    public void setDescription(String description){
        if(poiFragment != null)
            poiFragment.setDescription(description);
    }

    public void calculateDistance(){
        if(App.getLastLocation() == null || target == null)
            return;

        if(poiFragment != null)
            poiFragment.setDistance(App.getLastLocation().distanceTo(target));
    }

    @Override
    public void draw(@SuppressWarnings("NullableProblems") Canvas canvas) {

        if (!enabled) {
            super.draw(canvas);
            return;
        }

        if (lastOrientation == null)
            return;

        if (target == null)
            return;

        getLocationInWindow(location);
        float x = location[0];
        float y = location[1];

        int saveCount = canvas.save();

        if (!layeredViewQueue.isEmpty()) {
            throw new AssertionError("View queue is not empty.");
        }

        // We don't want to be rendered so seed the queue with our children.
        for (int i = 0, count = getChildCount(); i < count; i++) {
            LayeredView layeredView = layeredViewPool.obtain();
            layeredView.set(getChildAt(i), 0);
            layeredViewQueue.add(layeredView);
        }

        while (!layeredViewQueue.isEmpty()) {
            LayeredView layeredView = layeredViewQueue.removeFirst();
            View view = layeredView.view;
            int layer = layeredView.layer;

            // Restore the object to the pool for use later.
            layeredView.clear();
            layeredViewPool.restore(layeredView);

            // Hide any visible children.
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                visibilities.clear();
                for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
                    View child = viewGroup.getChildAt(i);
                    //noinspection ConstantConditions
                    if (child.getVisibility() == VISIBLE) {
                        visibilities.set(i);
                        child.setVisibility(INVISIBLE);
                    }
                }
            }


            int viewSaveCount = canvas.save();

            float verticalFOV = ArDisplayView.getCamera().getVerticalAngle();

            if (App.getLastLocation() == null) {
                ARLog.e("[%s]::[lastLocation was null]", TAG);
                return;
            }

            float curBearingToMW = App.getLastLocation().bearingTo(target);
            float horizontalFOV = ArDisplayView.getCamera().getHorizontalAngle();
//            ARLog.d("[%s]::[v: %s, h:%s]", TAG, verticalFOV, horizontalFOV);

            // use roll for screen rotation
            canvas.rotate((float) (0.0f - Math.toDegrees(lastOrientation[2])));
            // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
            float dx = (float) ((canvas.getWidth() / horizontalFOV) * (Math.toDegrees(lastOrientation[0]) - curBearingToMW));
            float dy = (float) ((canvas.getHeight() / verticalFOV) * Math.toDegrees(lastOrientation[1]));

            // wait to translate the dx so the horizon doesn't get pushed off
            canvas.translate(0.0f, 0.0f - dy);

            final Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            contentPaint.setColor(Color.BLUE);

            // make our line big enough to draw regardless of rotation and translation
//      canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, contentPaint);

            // now translate the dx
            canvas.translate(0.0f - dx, 0.0f);

            // draw our point -- we've rotated and translated this to the right spot already
//      canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 8.0f, contentPaint);


//    camera.save();
//    camera.rotate(rotationX, rotationY, 0);
//    camera.getMatrix(matrix);
//    camera.restore();
//
//    matrix.preTranslate(-cx, -cy);
//    matrix.postTranslate(cx, cy);
//    canvas.concat(matrix);
//    canvas.scale(zoom, zoom, cx, cy);
//

//
            // Scale the layer index translation by the rotation amount.
//      float translateShowX = rotationY / ROTATION_MAX;
//      float translateShowY = rotationX / ROTATION_MAX;
//      float tx = layer * spacing * density * translateShowX;
//      float ty = layer * spacing * density * translateShowY;
//      canvas.translate(tx, -ty);

            view.getLocationInWindow(location);
            canvas.translate(location[0] - x, location[1] - y);

            if (shouldZoom)
                canvas.scale(2.0f, 2.0f, x, y);

            if (drawViews) {
                view.draw(canvas);
            }

            canvas.restoreToCount(viewSaveCount);

            // Restore any hidden children and queue them for later drawing.
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
                    if (visibilities.get(i)) {
                        View child = viewGroup.getChildAt(i);
                        //noinspection ConstantConditions
                        child.setVisibility(VISIBLE);
                        LayeredView childLayeredView = layeredViewPool.obtain();
                        childLayeredView.set(child, layer + 1);
                        layeredViewQueue.add(childLayeredView);
                    }
                }
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private String nameForId(int id) {
        String name = idNames.get(id);
        if (name == null) {
            try {
                name = res.getResourceEntryName(id);
            } catch (NotFoundException e) {
                name = String.format("0x%8x", id);
            }
            idNames.put(id, name);
        }
        return name;
    }

    private static abstract class Pool<T> {
        private final Deque<T> pool;

        Pool(int initialSize) {
            pool = new ArrayDeque<>(initialSize);
            for (int i = 0; i < initialSize; i++) {
                pool.addLast(newObject());
            }
        }

        T obtain() {
            return pool.isEmpty() ? newObject() : pool.removeLast();
        }

        void restore(T instance) {
            pool.addLast(instance);
        }

        protected abstract T newObject();
    }
}
