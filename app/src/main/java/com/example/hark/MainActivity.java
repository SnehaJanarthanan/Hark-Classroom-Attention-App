package com.example.hark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String currentAppPackage;
    private static final String TAG = "Tier1";

    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button tier1SelectButton = findViewById(R.id.tier1selectbutton);
        Button tier1selectbutton2 = findViewById(R.id.tier1selectbutton2);



        tier1SelectButton.setOnClickListener(new View.OnClickListener() {


            @Override

            public void onClick(View v) {
                tier1selectbutton2.setEnabled(false);

                // Enable button2 after 60 minutes
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tier1selectbutton2.setEnabled(true);
                    }
                }, 60 * 60 * 1000);
                startFunctionWithInterval();

            }

        });



        tier1selectbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printUsageStats();

            }
        });

    }

    private void printUsageStats() {

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        long startTime = System.currentTimeMillis() - 3600000; // 1 hour ago
        long endTime = System.currentTimeMillis();

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        long totalTimeInForeground = 0;

        if (usageStatsList.size() > 0) {
            for (UsageStats usageStats : usageStatsList) {
                String packageName = usageStats.getPackageName();
                if (!packageName.equals(currentAppPackage)) {
                    totalTimeInForeground += usageStats.getTotalTimeInForeground();
                }
            }
        }

        // Convert total time to minutes
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeInForeground);

        TextView mtextView = findViewById(R.id.mtextView);


        mtextView.setText("Total time spent in other apps: " + minutes + " minutes.");
    }

    private void startFunctionWithInterval() {
        final int interval = 30000; // 30 seconds in milliseconds
        final int duration = 3600000; // 1 hour in milliseconds
        final long startTime = System.currentTimeMillis();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // This code will run every 30 seconds

                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= duration) {
                    // Stop the timer after 1 hour
                    timer.cancel();
                    return;
                }

                // Call your function here
                notifyevery5minutes();
            }
        }, 0, interval);
    }

    private void notifyevery5minutes() {
        Log.d("MyApp", "notifyevery5minutes() called");
        final int interval = 30000; // 30 seconds in milliseconds
        final int duration = 3600000; // 1 hour in milliseconds
        final long startTime = System.currentTimeMillis() - duration;

        final UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        final List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, System.currentTimeMillis());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Screen Usage";
            String description = "Screen usage notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("screen_usage", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        long previousTotalTimeInForeground = 0;

        // Iterate through usage stats every 30 seconds and check if screen usage time has increased
        for (long currentTime = startTime + interval; currentTime <= System.currentTimeMillis(); currentTime += interval) {
            long totalTimeInForeground = 0;

            for (UsageStats usageStats : usageStatsList) {
                if (usageStats.getLastTimeUsed() >= (currentTime - interval) && usageStats.getLastTimeUsed() < currentTime) {
                    totalTimeInForeground += usageStats.getTotalTimeInForeground();
                }
            }

            if (totalTimeInForeground > previousTotalTimeInForeground) {
                // Screen usage time has increased, send notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "screen_usage")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("HARK")
                        .setContentText("Screen usage time has increased!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);


                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("My App","Able to send Notification");

                    return;
                }
                else
                {
                    Log.d("My App","Unable to send Notification");
                }
                notificationManager.notify(0, builder.build());
            }

            previousTotalTimeInForeground = totalTimeInForeground;

            // Stop checking after 1 hour
            if (currentTime >= startTime + duration) {
                break;
            }
        }
    }
}