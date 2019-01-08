package com.beauty.circleprogressbarproject;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private CircleProgressbar circleProgressbarFirst;
    private CircleProgressbar circleProgressbarSecond;
    private CircleProgressbar circleProgressbarThird;
    private TextView tvCountDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleProgressbarFirst = findViewById(R.id.cp_progressFirst);
        circleProgressbarSecond = findViewById(R.id.cp_progressSecond);
        circleProgressbarThird = findViewById(R.id.cp_progressThird);
        tvCountDown = findViewById(R.id.tv_countdown);

        circleProgressbarSecond.setDoughnutColors(1);
        circleProgressbarThird.setDoughnutColors(2);
        tvCountDown.setText((int) circleProgressbarThird.getProgress() + "");
        circleProgressbarThird.setOnProgressbarChangeListener(new CircleProgressbar.OnProgressbarChangeListener() {
            @Override
            public void onProgressChanged(CircleProgressbar circleSeekbar, float progress, boolean fromUser) {
                tvCountDown.setText((int) progress + "");
            }

            @Override
            public void onStartTracking(CircleProgressbar circleSeekbar) {

            }

            @Override
            public void onStopTracking(CircleProgressbar circleSeekbar) {

            }
        });
    }

    public void start(View view) {
        circleProgressbarFirst.setProgressWithAnimation(20, 6000);
        circleProgressbarSecond.setProgressWithAnimation(20, 6000);
        circleProgressbarThird.setProgressWithAnimation(20, 6000);
    }

    public void restore(View view) {
        circleProgressbarFirst.setProgress(100);
        circleProgressbarSecond.setProgress(100);
        circleProgressbarThird.setProgress(100);
        tvCountDown.setText((int) circleProgressbarThird.getMaxProgress() + "");
    }
}
