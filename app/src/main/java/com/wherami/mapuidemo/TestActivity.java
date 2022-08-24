package com.wherami.mapuidemo;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.wherami.mapuidemo.SpeedCalculator.Calibrator;
import com.wherami.mapuidemo.SpeedCalculator.MeasureData;
import com.wherami.mapuidemo.SpeedCalculator.MeasurePoint;
import com.wherami.mapuidemo.SpeedCalculator.Point;
import com.wherami.mapuidemo.SpeedCalculator.XYZAccelerometer;

import java.io.StreamCorruptedException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wherami.lbs.sdk.Client;

public class TestActivity extends AppCompatActivity {


    static final int TIMER_DONE = 2;
    static final int START = 3;
    static final int CAL_TIMER_DONE = 4;
    static final int ERROR = 5;

//    private StartCatcher mStartListener;
    private XYZAccelerometer xyzAcc;
    private SensorManager mSensorManager;
    public static final long UPDATE_INTERVAL = 200;
    public static final long MEASURE_TIMES = 10;
    private Timer timer = null;
    private TextView tv;
    private Button testBtn;
//    private int counter;
    private boolean shouldCalibrate = false;
    private boolean isStarted = false;

    private MeasureData mdXYZ;
//    private List<MeasurePoint> mspXYZ = new ArrayList<>();
    private Handler hRefresh = new Handler();
    private Runnable calSpeed = new Runnable() {
        @Override
        public void run() {
            onMeasureDone();
            String es1 = Float.toString(Math.round(mdXYZ.getLastSpeedKm()*100)/100f);
            String es2 = Float.toString(mdXYZ.getLastSpeed());
            tv.append(" END SPEED " + es1 + " " +es2 + " "+ System.currentTimeMillis()+" \n");
            if (mdXYZ.getLastSpeed() > 1.0F){

                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            }
//            mdXYZ = new MeasureData(UPDATE_INTERVAL);
////            counter = 0;
//            mdXYZ.setView(tv);
            hRefresh.postDelayed(this,1000);
        }
    };
    /** handler for async events*/


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tv = (TextView) findViewById(R.id.textViewTest);
        testBtn = (Button) findViewById(R.id.test_button);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonTest(v);
            }
        });
        tv.setMovementMethod(new ScrollingMovementMethod());
//        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound_file_1);
//        mediaPlayer.start();
//        shouldCalibrate = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv.append("\n ..");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        setAccelerometer();
//        setStartCatcher();
        mSensorManager.registerListener(xyzAcc,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(xyzAcc);
        super.onPause();
    }


    public void onButtonTest(View v) {
//        disableButtons();
        if (isStarted){
            isStarted=false;
//            hRefresh.sendEmptyMessage(TIMER_DONE);
            timer.cancel();
            timer.purge();
            timer=null;
            hRefresh.removeCallbacks(calSpeed);
        }else{
            isStarted=true;
            mdXYZ = new MeasureData(UPDATE_INTERVAL);
            //            counter = 0;
            mdXYZ.setView(tv);
//            if(shouldCalibrate){
//                tv.append("Calibrating");
//                shouldCalibrate = false;
//                Calibrator cal = new Calibrator(hRefresh, xyzAcc, START);
//                cal.calibrate();
//            }else{
//                hRefresh.sendEmptyMessage(START);
//            }
            tv.append(" START" +  System.currentTimeMillis() +" \n");
            if(timer == null) {
                timer = new Timer();
            }else
                timer.cancel();
            timer.scheduleAtFixedRate(
                    new TimerTask() {
                        public void run() {
                            mdXYZ.addPoint(xyzAcc.getPoint());
                        }
                    },
                    0,
                    UPDATE_INTERVAL);
            hRefresh.postDelayed(calSpeed,1000);
        }


    }

//    void dumpSensor() {
//        ++counter;
//        mdXYZ.addPoint(xyzAcc.getPoint());

//        hRefresh.sendEmptyMessage(99);

//        if (counter > MEASURE_TIMES) {
//            counter=0;
////            timer.cancel();
//            onMeasureDone();
//            String es1 = Float.toString(Math.round(mdXYZ.getLastSpeedKm()*100)/100f);
//            String es2 = Float.toString(mdXYZ.getLastSpeed());
//            tv.append(" END SPEED " + es1 + " " +es2 + " "+ System.currentTimeMillis()+" \n");
//            hRefresh.sendEmptyMessage(START);
//        }

//    }




    private void setAccelerometer() {
        xyzAcc = new XYZAccelerometer();
        mSensorManager.registerListener(xyzAcc,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    private void enableButtons() {
        testBtn.setEnabled(true);

    }
    private void disableButtons() {
        testBtn.setEnabled(false);
    }

    private void onMeasureDone() {
        try {
            mdXYZ.process();
//            long now = System.currentTimeMillis();
//            mdXYZ.saveExt(this, Long.toString(now) + ".csv");
        } catch (Throwable ex) {
            Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT);
        }
    }
}
