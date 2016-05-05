package edu.ucdenver.triton;


import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class SplashActivity extends Activity{

    TextView text;
    Switch switch3d;
    Button button;
    boolean is3D;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        is3D = false;

        switch3d = (Switch)findViewById(R.id.switch3D);
        button   = (Button)findViewById(R.id.launchButton);
        text     = (TextView)findViewById(R.id.splashText);

        Animation slowFade = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in);
        Animation fastFade = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in);
        Animation extraSlowFade = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in);
        slowFade.setDuration(2000);
        slowFade.setStartOffset(1000);
        extraSlowFade.setDuration(3000);
        extraSlowFade.setStartOffset(1500);
        fastFade.setDuration(2000);

        text.setAnimation(fastFade);
        switch3d.setAnimation(slowFade);
        button.setAnimation(extraSlowFade);


        button.setOnClickListener(new View.OnClickListener() {
            Intent i;
            @Override
            public void onClick(View v) {
                if (is3D) {
                    i = new Intent(getBaseContext(), GraphicsActivity.class);
                }
                else {
                    i = new Intent(getBaseContext(), MainActivity.class);
                }
                startActivity(i);
            }
        });

        switch3d.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                is3D = isChecked;
            }
        });
    }
}
