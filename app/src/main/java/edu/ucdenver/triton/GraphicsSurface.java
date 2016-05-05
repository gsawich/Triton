package edu.ucdenver.triton;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.widget.TextView;

public class GraphicsSurface extends GLSurfaceView {
    private final GraphicsRenderer glRenderer;

    public GraphicsSurface(Context context, AttributeSet attr){
        super(context, attr);
        setEGLContextClientVersion(2);
        glRenderer = new GraphicsRenderer(context);
        setRenderer(glRenderer);
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
