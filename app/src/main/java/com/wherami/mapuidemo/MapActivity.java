package com.wherami.mapuidemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wherami.mapuidemo.SpeedCalculator.MeasureData;
import com.wherami.mapuidemo.SpeedCalculator.XYZAccelerometer;

import java.util.Timer;
import java.util.TimerTask;

import wherami.lbs.sdk.core.MapEngine;
import wherami.lbs.sdk.data.Location;
import wherami.lbs.sdk.data.Poi;
import wherami.lbs.sdk.ui.MapFragment;

public class MapActivity extends AppCompatActivity implements
        MapFragment.MapEngineReadyCallback, //Optionally implements this interface to receive the MapEngine instance
        MapEngine.LocationUpdateCallback { //Optionally implements this interface to receive location update from the MapEngine instance

    private static final String TAG="MapActivity";

    private XYZAccelerometer xyzAcc;
    private SensorManager mSensorManager;
    public static final long UPDATE_INTERVAL = 200;
    public static final long MEASURE_TIMES = 10;
    private Timer timer = null;
    private boolean isStarted = false;
    private MeasureData mdXYZ;
    private Handler hRefresh = new Handler();
    private Runnable calSpeed = new Runnable() {
        @Override
        public void run() {
            onMeasureDone();
            String es1 = Float.toString(Math.round(mdXYZ.getLastSpeedKm()*100)/100f);
            String es2 = Float.toString(mdXYZ.getLastSpeed());
//            tv.append(" END SPEED " + es1 + " " +es2 + " "+ System.currentTimeMillis()+" \n");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        setAccelerometer();
//        setStartCatcher();
        mSensorManager.registerListener(xyzAcc,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStarted=false;
//            hRefresh.sendEmptyMessage(TIMER_DONE);
        timer.cancel();
        timer.purge();
        timer=null;
        hRefresh.removeCallbacks(calSpeed);
    }

    @Override
    public void onLocationUpdated(Location location) {
        Log.i(TAG, "onLocationUpdated: " + location.x +", " + location.y);
    }

    @Override
    public void onMapEngineReady(MapEngine mapEngine) {
        //Upon obtaining the MapEngine, attach the desired callback
        mapEngine.attachLocationUpdateCallback(this);

        mdXYZ = new MeasureData(UPDATE_INTERVAL);
        //            counter = 0;
//        mdXYZ.setView(tv);
//            if(shouldCalibrate){
//                tv.append("Calibrating");
//                shouldCalibrate = false;
//                Calibrator cal = new Calibrator(hRefresh, xyzAcc, START);
//                cal.calibrate();
//            }else{
//                hRefresh.sendEmptyMessage(START);
//            }
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
//            mdXYZ.setView(tv);
//            if(shouldCalibrate){
//                tv.append("Calibrating");
//                shouldCalibrate = false;
//                Calibrator cal = new Calibrator(hRefresh, xyzAcc, START);
//                cal.calibrate();
//            }else{
//                hRefresh.sendEmptyMessage(START);
//            }
//            tv.append(" START" +  System.currentTimeMillis() +" \n");
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
    private void setAccelerometer() {
        xyzAcc = new XYZAccelerometer();
        mSensorManager.registerListener(xyzAcc,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_UI);
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
