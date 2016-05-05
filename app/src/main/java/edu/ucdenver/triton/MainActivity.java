package edu.ucdenver.triton;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private SolarSystemView sys;
    TextView dd;
    SeekBar speed;
    ImageButton rewind;
    ImageButton pause;
    ImageButton play;
    ImageButton forward;
    TextView speedText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sys = (SolarSystemView)findViewById(R.id.gui);
        dd = (TextView) findViewById(R.id.date_display);
        sys.setTextView(dd);
        sys.setSpeed(1);
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
                sys.setSpeed((1+progress)*(progress+1));
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
                sys.setForwardBackward(-1);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sys.resume();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sys.pause();
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sys.setForwardBackward(1);
            }
        });
    }
}