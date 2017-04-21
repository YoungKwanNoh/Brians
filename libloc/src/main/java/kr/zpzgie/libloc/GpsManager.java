package kr.zpzgie.libloc;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.Observable;

/**
 * Created by brian on 2017-04-21.
 */

public class GpsManager extends Observable implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private String TAG = GpsManager.class.getName();
    private static GpsManager instance = null;

    public static final int MSG_REQUEST_PERMISSION =100;


    private Activity activity = null;
    private GoogleApiClient client = null;
    private LocationRequest locationRequest = null;

    private int interval = 10*1000;
    private int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    public static GpsManager getInstance(Activity activity){
        if (instance == null)
            instance = new GpsManager(activity);

        return instance;
    }

    public static GpsManager getInstance(){
        return instance;
    }

    public void close(){
        stop();
        instance = null;
    }



    private GpsManager(Activity activity){
        this.activity = activity;

        initLocationService();
        start();
    }

    private void initLocationService(){

        Log.d(TAG, "#GPS# initLocationService");

        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,  Manifest.permission.ACCESS_FINE_LOCATION)) {

//                Toast.makeText(activity, "권한설정이 필요합니다.", Toast.LENGTH_LONG).show();
            } else {


                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MSG_REQUEST_PERMISSION);

            }
        }



        if (client == null) {
            client = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    public void start(){

        if (client != null && client.isConnected() == false)
            client.connect();
    }

    public void stop(){

        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            client.disconnect();
        }

        client = null;

    }


    @Override
    @TargetApi(23)
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "#GPS# connect");

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (mLastLocation != null) {
            Log.d(TAG, "#GPS# LastLocation: " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude() + ", " + mLastLocation.getSpeed() + ", " + mLastLocation.getAccuracy());
        }

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(interval);
        locationRequest.setPriority(priority);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result =  LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(new LocationSettingResultCallback());

        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "#GPS# " + getLocationInfo(location));//location: " + location.getTime() + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getSpeed() + ", " + location.getAccuracy());

    }



    private String getLocationInfo(Location loc){
        return String.format("%d, %f, %f, %f", loc.getTime(), loc.getLatitude(), loc.getLongitude(), loc.getSpeed());
    }



    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "#GPS# Connection Failed: " + connectionResult.getErrorCode());

    }


    public class LocationSettingResultCallback implements ResultCallback<LocationSettingsResult> {
        @Override
        public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            final LocationSettingsStates state = result.getLocationSettingsStates();

            Log.d(TAG, "#GPS# location setting status: " + status.getStatusCode());
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(activity, 1000);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    break;
            }
        }
    }


}
