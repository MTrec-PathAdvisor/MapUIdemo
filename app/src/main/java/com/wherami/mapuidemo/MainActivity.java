package com.wherami.mapuidemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.StreamCorruptedException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wherami.lbs.sdk.Client;

public class MainActivity extends AppCompatActivity {
    private Button mainButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainButton = (Button) findViewById(R.id.main_button);

        String[] allPermissions = null;
        try {
            allPermissions = getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS)
                    .requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (allPermissions != null) {
            boolean allPermissionsAlreadyGranted = true;

            if (Build.VERSION.SDK_INT >= 23) {
                List<String> permissions2request = new ArrayList<>();
                for (String permission : allPermissions) {
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsAlreadyGranted = false;
                        permissions2request.add(permission);
                    }
                }

                if (!permissions2request.isEmpty()) {
                    ActivityCompat.requestPermissions(this,
                            permissions2request.toArray(new String[0]),
                            0xFFF);
                }
            }

            if(allPermissionsAlreadyGranted){
                initialize();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        initialize();
    }

    private void initialize(){
        try {
            // The Dataset is downloaded from a server
            // This is a development server serves as debug purpose only
            // One should setup a http(s) server and host the files under
            // http(s)://<host>/generated_assets/SmartIDCentre/offline_data/
            Client.Configure("http://43.252.40.60", "HKUST_fusion", this);
            Map<String, Object> config = new HashMap<>();
            config.put("wherami.lbs.sdk.core.MapEngineFactory:EngineType", "wherami.lbs.sdk.core.NativeMapEngine");
            Client.ConfigExtra(config);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        }


        checkDataUpdate();
    }

    private void checkDataUpdate(){
        //Start only when the app has the latest data
        mainButton.setEnabled(false);
        mainButton.setText("Checking Update...");
        Client.CheckDataUpdate(new Client.DataUpdateQueryCallback() {
            @Override
            public void onQueryFailed(Exception e) {
                Toast.makeText(MainActivity.this, "Failed to check data update", Toast.LENGTH_LONG);
                if(Client.GetDataVersion() != null){
                    //It is possible to continue by using old data
                    mainButton.setEnabled(true);
                    mainButton.setText("START");
                }else{
                    //If no data is downloaded previously, it is impossible to continue. Please retry under network environment
                }
            }

            @Override
            public void onUpdateAvailable(String s) {
                mainButton.setText("Updating data...");

                Client.UpdateData(new Client.DataUpdateCallback() {
                    @Override
                    public void onProgressUpdated(int i) {

                    }

                    @Override
                    public void onCompleted() {
                        Toast.makeText(MainActivity.this, "Update succeeded", Toast.LENGTH_SHORT);
                        mainButton.setEnabled(true);
                        mainButton.setText("START");
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to update data", Toast.LENGTH_LONG);
                        if(Client.GetDataVersion() != null){
                            //It is possible to continue by using old data
                            mainButton.setEnabled(true);
                            mainButton.setText("START");
                        }else{
                            //If no data is downloaded previously, it is impossible to continue. Please retry under network environment
                        }
                    }
                }, MainActivity.this);
            }

            @Override
            public void onLatestVersion() {
                Toast.makeText(MainActivity.this, "On latest version", Toast.LENGTH_SHORT);
                mainButton.setEnabled(true);
                mainButton.setText("START");
            }
        }, this);
    }

    public void onButtonClicked(View view){
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }
}
