package edu.ucdenver.triton;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

public class GraphicsSurface extends GLSurfaceView {
    private final GraphicsRenderer glRenderer;
    private final ScaleGestureDetector scaleDetector;
    public GraphicsSurface(Context context, AttributeSet attr){
        super(context, attr);
        setEGLContextClientVersion(2);
        glRenderer = new GraphicsRenderer(context);
        setRenderer(glRenderer);
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        float span;
        float scaleFactor;
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector){
            scaleFactor = 1;
            span = detector.getCurrentSpan();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //scaleFactor = detector.getScaleFactor();
            scaleFactor *= 1+((1-(span/detector.getCurrentSpan() ))/100);
            if (scaleFactor < 0.1f) { scaleFactor = 0.1f; }
            if (scaleFactor > 1.2f) { scaleFactor = 1.2f; }
            Log.i("ScaleFactor", "Scale Factor: " + scaleFactor );
            glRenderer.setScale(scaleFactor);
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        return true;
    }

    public void setText(TextView text) {
        glRenderer.setText(text);
    }

    public void speed(int speed) {
        glRenderer.setSpeed(speed);
    }

    public void resume() {
        glRenderer.resume();
    }

    public void pause() {
        glRenderer.pause();
    }

    public void forward() {
        glRenderer.setSpeed(Math.abs(glRenderer.getSpeed()));
    }
    public void reverse() {
        glRenderer.setSpeed((-1)*(glRenderer.getSpeed()));
    }
}
