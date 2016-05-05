package edu.ucdenver.triton;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
//import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ZoomButton;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GraphicsActivity extends FragmentActivity {

    protected GraphicsSurface view3d;
    public static int width, height;

    TextView dd;
    ZoomButton zoomOut;
    ZoomButton zoomIn;
    SeekBar speed;
    ImageButton rewind;
    ImageButton pause;
    ImageButton play;
    ImageButton forward;
    TextView speedText;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphics);

        view3d = (GraphicsSurface)findViewById(R.id.glsurface);
        dd = (TextView) findViewById(R.id.date_display);
        view3d.setText(dd);
        zoomOut = (ZoomButton) findViewById(R.id.zoom_out);
        zoomIn = (ZoomButton) findViewById(R.id.zoom_in);
        speed = (SeekBar) findViewById(R.id.speed_bar);
        speedText = (TextView) findViewById(R.id.speed_text);
        speedText.setText(getResources().getString(R.string.speed_display, 1));
        rewind = (ImageButton) findViewById(R.id.rewind);
        pause = (ImageButton) findViewById(R.id.pause);
        play = (ImageButton) findViewById(R.id.play);
        forward = (ImageButton) findViewById(R.id.forward);

        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedText.setText(getResources().getString(R.string.speed_display, progress + 1));
                view3d.speed((1+progress)*(progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view3d.reverse();
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view3d.resume();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view3d.pause();
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view3d.forward();
            }
        });

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
        view3d.onResume();
    }

    @Override
    protected void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        view3d.onPause();
    }
    protected void onStop() {
        super.onStop();
    }
}
