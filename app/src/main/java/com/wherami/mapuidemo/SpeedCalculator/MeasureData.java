package com.wherami.mapuidemo.SpeedCalculator;

import android.content.Context;
import android.widget.TextView;

import com.wherami.mapuidemo.MapActivity;
import com.wherami.mapuidemo.TestActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

public class MeasureData {
    // points from accelerometr
    private LinkedList<Point> accData;
    private LinkedList<MeasurePoint> data;

    // timer interval of generating points
    private long interval;
    private TextView view;

    public MeasureData(long interval) {
        this.interval = interval;
        accData = new LinkedList<Point>();
        data = new LinkedList<MeasurePoint>();
    }

    public void addPoint(Point p){
        if (accData.size()> MapActivity.MEASURE_TIMES){
            accData.removeFirst();
        }
        accData.addLast(p);
    }

    public void process(){

        for(int i = 0; i < accData.size(); ++i){
            Point p = accData.get(i);
            float speed = 0.0F;

            if(i > 0){
                speed = data.get(i-1).getSpeedAfter();
            }
            if (data.size()>MapActivity.MEASURE_TIMES){
                data.removeFirst();
            }
            data.add(new MeasurePoint(p.getX(), p.getY(), p.getZ(), speed, interval, getAveragePoint()));
            if (view!=null){
                String speedstr = Float.toString(speed);
                view.append(i+": "+speedstr+" \n");
            }
        }
    }

    public boolean saveExt(Context con, String fname) throws Throwable {

        try {

            File file = new File(con.getExternalFilesDir(null), fname);
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(os);


            for (int i = 0; i < data.size(); ++i) {
                MeasurePoint m = data.get(i);
                out.write(m.getStoreString());
            }

            out.close();
        } catch (Throwable t) {
            throw (t);
        }
        return true;
    }

    private Point getAveragePoint() {
        float x = 0;
        float y = 0;
        float z = 0;

        for(int i = 0; i < accData.size(); ++i){
            Point p = accData.get(i);
            x += p.getX();
            y += p.getY();
            z += p.getZ();
        }

        return new Point(x, y, z, 1);
    }

    public float getLastSpeed(){
        return data.getLast().getSpeedAfter();
    }

    public float getLastSpeedKm(){
        float ms = getLastSpeed();
        return ms*3.6f;
    }

    public void setView(TextView tv) {
        this.view =tv;
    }
}