package com.example.backgroundlocation;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static android.content.Context.NOTIFICATION_SERVICE;

public class LocationWork extends Worker {

    private NotificationManager notificationManager;
    private Context context;
    String progress = "Starting work...";
    int NOTIFICATION_ID = 1;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;

    public LocationWork(@NonNull @org.jetbrains.annotations.NotNull Context context,
                        @NonNull @org.jetbrains.annotations.NotNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        locationManager = LocationManager.getInstance(context);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("local_broadcast");
        LocalBroadcastManager.getInstance(context).registerReceiver(localBroadCastReceiver,
                localBroadcastIntentFilter);
    }


    @NonNull @org.jetbrains.annotations.NotNull @Override public Result doWork() {
        setForegroundAsync(showNotification(progress));
        while (true) {
            if (1 > 2) {
                break;
            }
            locationManager.startLocationUpdates();

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Result.success();
    }

    @NonNull
    private ForegroundInfo showNotification(String progress) {
        return new ForegroundInfo(NOTIFICATION_ID, createNotification(progress));
    }

    private Notification createNotification(String progress) {
        String CHANNEL_ID = "100";
        String title = "Foreground Work";
        String cancel = "Cancel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, title,
                            NotificationManager.IMPORTANCE_HIGH));
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(progress)
                .setSmallIcon(R.drawable.ic_android)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();

        return notification;
    }

    private void updateNotification(String progress) {
        Notification notification = createNotification(progress);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    BroadcastReceiver localBroadCastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Log.d("TAG","Broadcasted");
            progress = intent.getStringExtra("location");
            updateNotification(progress);
        }
    };

    @Override public void onStopped() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(localBroadCastReceiver);
        super.onStopped();
    }
}
