package com.vv.buildstuff.geofencemonitoring;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

/**
 * Created by vvennava on 10/20/14.
 */
public class RegionMonitorService extends Service {
    public static final String ACTION_INIT = "com.vv.buildstuff.geofencemonitoring.ACTION_INIT";
    private static final int NOTE_ID = 100;
    private static final String TAG = "RegionMonitorService";
    private NotificationManager mNoteManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNoteManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //Post a system notification when the service starts
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("Geofence service");
        builder.setContentText("Waiting for transition...");
        builder.setOngoing(true);

        Notification note = builder.build();
        mNoteManager.notify(NOTE_ID, note);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Noting to do yet, the service is being started
        if (ACTION_INIT.equals(intent.getAction())){
            //Do not care if the service dies unexpectedly 
            return START_NOT_STICKY ;
        }
        
        if (LocationClient.hasError(intent)) {
            //Log any errors 
            Log.w(TAG, "Error monitoring region:" + LocationClient.getErrorCode(intent));
        }else {
            //Update the ongoing notification from the new event
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
            builder.setOngoing(true);

            int transitionType = LocationClient.getGeofenceTransition(intent);

            //Check if the transition type is enter or exit
            if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
                builder.setContentTitle("Geofence transition");
                builder.setContentText("Entering the Geofence");
            }else if(transitionType == Geofence.GEOFENCE_TRANSITION_EXIT){
                builder.setContentTitle("Geofence transition");
                builder.setContentText("Exiting the Geofence");
            }

            Notification note = builder.build();
            mNoteManager.notify(NOTE_ID, note);

        }

        //Do not care if the service dies unexpectedly
        return START_NOT_STICKY ;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Cancel the outgoing notifications
        mNoteManager.cancel(NOTE_ID);
    }
}
