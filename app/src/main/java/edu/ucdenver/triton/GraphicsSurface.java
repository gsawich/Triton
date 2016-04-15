package edu.ucdenver.triton;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GraphicsSurface extends GLSurfaceView {
    private final GraphicsRenderer glRenderer;

    public GraphicsSurface(Context context){
        super(context);
        setEGLContextClientVersion(2);
        glRenderer = new GraphicsRenderer();
        setRenderer(glRenderer);
    }
}
