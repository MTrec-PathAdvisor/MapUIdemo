package com.wherami.mapuidemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import wherami.lbs.sdk.core.MapEngine;
import wherami.lbs.sdk.data.Location;
import wherami.lbs.sdk.data.Poi;
import wherami.lbs.sdk.ui.MapFragment;

public class MapActivity extends AppCompatActivity implements
        MapFragment.MapEngineReadyCallback, //Optionally implements this interface to receive the MapEngine instance
        MapEngine.LocationUpdateCallback { //Optionally implements this interface to receive location update from the MapEngine instance

    private static final String TAG="MapActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }

    @Override
    public void onLocationUpdated(Location location) {
        Log.i(TAG, "onLocationUpdated: " + location.x +", " + location.y);
    }

    @Override
    public void onMapEngineReady(MapEngine mapEngine) {
        //Upon obtaining the MapEngine, attach the desired callback
        mapEngine.attachLocationUpdateCallback(this);
    }
}
