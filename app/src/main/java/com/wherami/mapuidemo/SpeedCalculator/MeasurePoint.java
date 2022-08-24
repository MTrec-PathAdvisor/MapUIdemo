package com.wherami.mapuidemo.SpeedCalculator;

import static android.content.ContentValues.TAG;

import android.util.Log;

public class MeasurePoint {
    private float x;
    private float y;
    private float z;
    private float speedBefore;
    private float speedAfter;
    private float distance;
    private float acceleration;
    private long interval;
    private Point averagePoint;

    public MeasurePoint(float x, float y, float z, float speedBefore, long interval, Point averagePoint) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.speedBefore = speedBefore;
        this.interval = interval;
        this.averagePoint = averagePoint;
        speedAfter = 0;
        calc();
    }

    private void calc(){
        //Acceleration as projection of current vector on average
        acceleration = this.x*averagePoint.getX() +
                this.y*averagePoint.getY() +
                this.z*averagePoint.getZ();
        acceleration = acceleration / ((float)Math.sqrt(averagePoint.getForce()));
//        acceleration = (float) Math.sqrt(x*x+y*y+z*z);
        float t = (float)(interval / 1000f); // in s
        speedAfter = speedBefore + acceleration * t; // vt = v(t-1) + a * deltaT
        distance = speedBefore*t + acceleration*t*t/2; // v(t-1)*t + a*t*t/2
        Log.d(TAG, "calc: acce "+String.valueOf(acceleration));
//        Log.d(TAG, "calc: xyz "+String.valueOf(x)+" "+String.valueOf(y)+" "+String.valueOf(z)+" ");
//        Log.d(TAG, "calc: u v "+String.valueOf(speedBefore)+" -> "+String.valueOf(speedAfter));

    }

    public String getStoreString(){
        String s = x +","+y+","+z+":a="+acceleration+" u="+speedBefore+" v="+speedAfter+" d="+distance ;
        return s;
    }

// add getters

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getSpeedBefore() {
        return speedBefore;
    }

    public float getSpeedAfter() {
        return speedAfter;
    }

    public float getDistance() {
        return distance;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public long getInterval() {
        return interval;
    }

    public Point getAveragePoint() {
        return averagePoint;
    }
}