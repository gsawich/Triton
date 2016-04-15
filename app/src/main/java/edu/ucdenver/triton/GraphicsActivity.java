package edu.ucdenver.triton;

import android.app.Activity;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

public class GraphicsActivity extends Activity {

    private GLSurfaceView view;
    public static int width, height;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new GraphicsSurface(this);
        setContentView(view);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x/2;
        height = size.y/2;
    }
    @Override
    protected void onResume()
    {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        view.onResume();
    }

    @Override
    protected void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        view.onPause();
    }
    protected void onStop() {
        super.onStop();
    }
}
