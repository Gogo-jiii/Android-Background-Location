package com.example.backgroundlocation;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public Activity activty;
    Button btnAskForegroundPermissions, btnGetBackgroundLocation;
    private final String[] foreground_location_permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private final String[] background_location_permission = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private PermissionManager permissionManager;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activty = MainActivity.this;
        btnAskForegroundPermissions = findViewById(R.id.btnAskForegroundPermissions);
        btnGetBackgroundLocation = findViewById(R.id.btnGetBackgroundLocation);
        permissionManager = PermissionManager.getInstance(this);
        locationManager = LocationManager.getInstance(this);

        btnAskForegroundPermissions.setOnClickListener(v -> {
            if (!permissionManager.checkPermissions(foreground_location_permissions)) {
                permissionManager.askPermissions(MainActivity.this, foreground_location_permissions, 100);
            }
        });

        btnGetBackgroundLocation.setOnClickListener(view -> {
            if (!permissionManager.checkPermissions(background_location_permission)) {
                permissionManager.askPermissions(MainActivity.this, background_location_permission, 200);
            } else {
                if (locationManager.isLocationEnabled()) {
                    startLocationWork();
                } else {
                    locationManager.createLocationRequest();
                    Toast.makeText(MainActivity.this, "Location service is not enabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(MainActivity.this, 100, permissions,
                grantResults)) {
            Log.d("TAG", "1");
            if (locationManager.isLocationEnabled()) {
                startLocationWork();
            } else {
                locationManager.createLocationRequest();
                Toast.makeText(MainActivity.this, "Location service is not enabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationWork() {
        OneTimeWorkRequest foregroundWorkRequest = new OneTimeWorkRequest.Builder(LocationWork.class)
                .addTag("LocationWork")
                .setBackoffCriteria(BackoffPolicy.LINEAR,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(MainActivity.this).enqueue(foregroundWorkRequest);
    }
}