package com.paipeng.checkin.location;

import static com.paipeng.checkin.location.GoogleLocationService.LOCATION_STATE.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class GoogleLocationService implements LocationServiceInterface {
    private static final String TAG = GoogleLocationService.class.getSimpleName();
    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    private Location location; // location

    protected CLocation cLocation;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds

    // Declaring a Location Manager

    private final GoogleLocationListenner googleLocationListenner;

    public GoogleLocationService(Context context, String apiKey, int gpsIntervalTime, Language language) {
        this.context = context;
        this.apiKey = apiKey;
        this.gpsIntervalTime = gpsIntervalTime;
        this.language = language;
        this.locationState = LOCATION_STATE_NONE;

        this.gpsIntervalTime = 1;
        googleLocationListenner = new GoogleLocationListenner();
    }

    protected Context context;
    protected int gpsIntervalTime = 15;//定位时间间隔，单位s，初始化数值是默认定位时间间隔
    protected Language language;
    protected String apiKey;
    private LOCATION_STATE locationState;



    private void updateLocationState(LOCATION_STATE locationState) {
        if (this.locationState != locationState) {
            this.locationState = locationState;

        }
    }

    @Override
    public void searching() {

    }
    @Override
    public void started() {
        updateLocationState(LOCATION_STATE_STARTED);
    }

    @Override
    public void stopped() {
        updateLocationState(LOCATION_STATE_STOPPED);

    }


    public CLocation getLocation() {
        return cLocation;
    }

    public void setCLocation(CLocation cLocation) {
        this.cLocation = cLocation;
    }


    @SuppressLint("MissingPermission")
    public void startLocation () {
        Log.i(TAG, "startLocation");
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.i(TAG, "isGPSEnabled: " + isGPSEnabled + " isNetworkEnabled: " + isNetworkEnabled);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Log.d(TAG, "no location provider is enabled");
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            gpsIntervalTime*1000,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, googleLocationListenner);

                    searching();
                    Log.d(TAG, "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null && location.getLatitude() != 0 && location.getLongitude() != 0) {
                            setLocation(location);
                            started();
                        }
                    }
                } else if (isGPSEnabled) {
                    // if GPS Enabled get lat/long using GPS Services
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            gpsIntervalTime*1000,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, googleLocationListenner);

                    searching();
                    Log.d(TAG, "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (location != null && location.getLatitude() != 0 && location.getLongitude() != 0) {
                            setLocation(location);
                            started();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            stopped();
        }
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public void start() {
        Log.i(TAG, "startLocationListener");

        startLocation();
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */

    @Override
    @SuppressLint("MissingPermission")
    public void stop(){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager != null){
            locationManager.removeUpdates(googleLocationListenner);
            stopped();
        }
    }


    private void setLocation(Location location) {
        if (cLocation == null) {
            cLocation = new CLocation();
        }
        Log.i(TAG, "googleLocation: " + location.getLatitude() + ", " + location.getLongitude() + " " + location.toString());
        location.setLatitude(location.getLatitude());
        location.setLongitude(location.getLongitude());

        Locale local;
        Geocoder geocoder;
        List<Address> addresses;

        Log.i(TAG, "current location: " + language);
        if (Language.LANGUAGE_ZH.equals(language)) {
            Log.i(TAG, "set location to ch");
            local = Locale.SIMPLIFIED_CHINESE;
        } else if (Language.LANGUAGE_EN.equals(language)) {
            Log.i(TAG, "set location to en");
            local = Locale.ENGLISH;
        } else {
            Log.i(TAG, "set location to en");
            local = Locale.ENGLISH;
        }

        geocoder = new Geocoder(context, local);
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            Log.i(TAG, " Geocoder: " + addresses.get(0).toString());
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            cLocation.setCountry(country);
            cLocation.setProvince(state);
            cLocation.setCity(city);
            cLocation.setDistrict(addresses.get(0).getLocality());
            cLocation.setAddress(address);
        } catch (IOException e) {
            Log.e(TAG, "Geocoder exception: " + e.getMessage());
        }
    }


    public class GoogleLocationListenner implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.i(TAG, "onLocationChanged: " + location.toString());
            if (location != null) {
                setLocation(location);
                started();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }

    public enum Language {
        LANGUAGE_NO_SET,
        LANGUAGE_ZH,
        LANGUAGE_EN,
        LANGUAGE_FR,
        LANGUAGE_DE,
        LANGUAGE_JP
    }

    public enum LOCATION_STATE {
        LOCATION_STATE_NONE,
        LOCATION_STATE_STARTED,
        LOCATION_STATE_STOPPED,
        LOCATION_STATE_SEARCHING
    }
}
