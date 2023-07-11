package PedroFernandes.NSS;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Arrays;

public class OpenGLView extends GLSurfaceView {

    private OpenGLRenderer openGLRenderer;

    private final float TOUCH_SCALE_FACTOR = 1.0f/320;
    private float previousX;
    private float previousY;
    private float[] orientationAngles;


    public OpenGLView(Context context) {
        super(context);
        init(context);
    }

    public OpenGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){

        setEGLContextClientVersion(2);
        openGLRenderer = new OpenGLRenderer();
        openGLRenderer.setContext(context);
        setPreserveEGLContextOnPause(true);
        
        setRenderer(openGLRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        Log.d("Test", Arrays.toString(orientationAngles));

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()){
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;
                openGLRenderer.updateView(-dy * TOUCH_SCALE_FACTOR, dx * TOUCH_SCALE_FACTOR);
                break;
        }

        previousX = x;
        previousY = y;
        return true;
    }

    public void setOrientationArray(float[] orientationAngles) {
        this.orientationAngles = orientationAngles;
        openGLRenderer.setOrientationAngles(orientationAngles);
    }

    public void setInputsList(InputData[] inputDataList, int[] isDirty) {
        openGLRenderer.setInputsList(inputDataList, isDirty);
    }
}
