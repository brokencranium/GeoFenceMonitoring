package com.vv.buildstuff.geofencemonitoring;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;


public class MainActivity extends Activity
        implements SeekBar.OnSeekBarChangeListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener {
    private static final String FENCE_ID = "com.vv.buildstuff.geofencemonitoring.fence";
    private static final String TAG = "RegionMonitorActivity";
    private TextView mStatusText;
    private TextView mRadiusText;
    private SeekBar mRadiusSlider;
    private LocationClient mLocationClient;
    private Intent mServiceIntent;
    private PendingIntent mCallbackIntent;
    private Geofence mCurrentFence;

    //Main Map
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Wire up the UI connections
        mStatusText = (TextView) findViewById(R.id.status);
        mRadiusText = (TextView) findViewById(R.id.radius_text);
        mRadiusSlider = (SeekBar) findViewById(R.id.radius);
        mRadiusSlider.setOnSeekBarChangeListener(this);
        updateRadiusDisplay();

        //Create a client for Google Services
        mLocationClient = new LocationClient(this, this, this);

        //Create an intent to trigger this         
        mServiceIntent = new Intent(this, RegionMonitorService.class);

        //Create a pending intent for the google services callback
        mCallbackIntent = PendingIntent.getService(this, 0, mServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private void updateRadiusDisplay() {
        mRadiusText.setText(mRadiusSlider.getProgress() + " meters");
    }

    public void onSetGeofenceClick(View v) {
        //Obtain the last coordinate from services and radius from the UI
        Location current = mLocationClient.getLastLocation();
        int radius = mRadiusSlider.getProgress();

        //Create a new geo fence using the builder
        Geofence.Builder builder = new Geofence.Builder();
        mCurrentFence = builder.setRequestId(FENCE_ID)
                .setCircularRegion(current.getLatitude(), current.getLongitude(), radius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
        //Set the text with the lat and long values
        mStatusText.setText(String.format("Geo fence set at %.3f. %.3f", current.getLatitude(), current.getLongitude()));

    }

    public void onStartMonitorClick(View view) {
     if (mCurrentFence == null){
         Toast.makeText(this, "Geofence not yet set", Toast.LENGTH_SHORT).show();
         return;
     }
     //Add the fence to the start tracking, the pending intent wil be triggered with
     //new updates
        ArrayList<Geofence> geofences = new ArrayList<Geofence>();
        geofences.add(mCurrentFence);
        mLocationClient.addGeofences(geofences,mCallbackIntent,this);
    }

    public void onStopMonitorClick(View view) {
        //Remove to stop tracking
        mLocationClient.removeGeofences(mCallbackIntent,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Connect to all services
        if ((!mLocationClient.isConnected()) &&
        (!mLocationClient.isConnecting())){
            mLocationClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disconnect when not in foreground
        mLocationClient.disconnect();
    }

    // Seek Bar

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateRadiusDisplay();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //Connection Cal backs
    @Override
    public void onConnected(Bundle bundle) {
      Log.v(TAG, "Google services connected");
    }

    @Override
    public void onDisconnected() {
        Log.w(TAG, "Google services disconnected");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Google connection services failed");

    }

    // On add geo fence
    /**
     * Called when the asynchronous geofence add is complete.
     * we start monitoring the service
     */
    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
      if(statusCode == LocationStatusCodes.SUCCESS){
          Toast.makeText(this,"Geofence added successfully", Toast.LENGTH_SHORT).show();
      }
        Intent startIntent = new Intent(mServiceIntent);
        startIntent.setAction(RegionMonitorService.ACTION_INIT);
        startService(mServiceIntent);

    }



    /**
     * Called when asynchronous geofence remove is complete.
     * The version called depends on whether you requested the
     * removal via PendingIntent or request Id.
     * When this occurs, the monitoring service is stopped
     */

    // On remove geo fence

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
        if (statusCode == LocationStatusCodes.SUCCESS){
            Toast.makeText(this, "Geo fence removed successfully",Toast.LENGTH_SHORT).show();
        }
        stopService(mServiceIntent);

    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
        if (statusCode == LocationStatusCodes.SUCCESS){
            Toast.makeText(this, "Geo fence removed successfully",Toast.LENGTH_SHORT).show();
        }
        stopService(mServiceIntent);

    }



//

}
