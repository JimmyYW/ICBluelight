package me.jimmywang.icbluelight;

/**
 * This is a fragment contains a map fragment. This fragment create and push event to database and update user location
 * both on map and to database. The map view will follow user location. Once user click safe, fragment ended.
 * Created by yanmingwang on 12/10/16.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static me.jimmywang.icbluelight.LoginActivity.PREFS_NAME;

public class HelpUpdateFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {
    private onSafe callback;
    private Event thisEvent;
    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean firstupdate;
    private SharedPreferences settings;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private View view;

    private static final LatLng IC = new LatLng(42.422121, -76.494127);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    /**
     * A Callback once student report safe
     */
    public interface onSafe{
        void onSafe();
    }

    public HelpUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_help_update, container, false);
        Button safe = (Button) view.findViewById(R.id.imsafenow);
        safe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSafe();
            }
        });

        //Set googleAPI
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //start map
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        //start location update
        createLocationRequest();

        firstupdate = true;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //get additional info
        Bundle arguments = getArguments();
        String temp1 ="None";
        String temp2 = "None";
        if(arguments != null)
        {
            temp1 = arguments.getString("MS");
            temp2 = arguments.getString("DT");
        }

        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        String nameTmep = settings.getString("username", "Name");

        //Create new event or resume to the old one
        if (settings.contains("evenId")){
            Log.e("LOG_EORROR", settings.getString("evenId", "no ID 1") + "F1");
            thisEvent = new Event(mDatabase,settings);
        }else {
            Log.e("LOG_EORROR", settings.getString("evenId", "no ID") + "F2");
            thisEvent = new Event(mDatabase, "00.00", "00.00", nameTmep,temp1,temp2,settings);
        }

        return view;
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * This makes sure that the container activity has implemented
     * the callback interface. If not, it throws an exception
     * @param activity
     * current activity that attach to
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (onSafe) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

    /**
     * When this fragment is destroyed, the nested fragment need to be remove or will cause memory leak
     */
    @Override
    public void onDestroyView() {
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            getActivity().getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }

        if (!settings.contains("evenId")) {
            thisEvent.delete();
        }

        super.onDestroyView();
    }


    /**
     * Callback method use by google api
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //start location update
        startLocationUpdates();
        //start database update
        if(mCurrentLocation!=null){
            thisEvent.update(String.valueOf(mCurrentLocation.getLongitude()),String.valueOf(mCurrentLocation.getLatitude()));
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Callback method use by google map api when user location changed
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        double lat1;
        double latNow;
        double lng1;
        double lngNow;
        mCurrentLocation = location;

        /**
         * if is first update don't count distance
         */
        if (firstupdate && mCurrentLocation!=null) {
            latNow = mCurrentLocation.getLatitude();
            lngNow = mCurrentLocation.getLongitude();
            lat1 = mCurrentLocation.getLatitude();
            lng1 = mCurrentLocation.getLongitude();
            thisEvent.update(String.valueOf(mCurrentLocation.getLongitude()),String.valueOf(mCurrentLocation.getLatitude()));

        } else {
            lat1 = mLastLocation.getLatitude();
            lng1 = mLastLocation.getLongitude();
            latNow = mCurrentLocation.getLatitude();
            lngNow = mCurrentLocation.getLongitude();
        }

        /**
         * If there is accuracy and user moved 0.005 mile from last location update position to database
         */
        int suitableMeter = 35; // adjust your need
        if (mCurrentLocation.hasAccuracy()  && mCurrentLocation.getAccuracy() <= suitableMeter){
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            if (distance(lat1, lng1, latNow, lngNow) > 0.005) {
                thisEvent.update(String.valueOf(mCurrentLocation.getLongitude()),String.valueOf(mCurrentLocation.getLatitude()));
            }
        }

        if (firstupdate) {
            mLastLocation = mCurrentLocation;
            firstupdate = false;
        } else {
            mLastLocation = mCurrentLocation;
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        enableMyLocation();
    }

    /**
     * Create location request
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        //update every sec
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            /*
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            */
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            Log.v("enableMyLocation: ", "Permission granted");
        }
    }

    /**
     * This is a helper function used to calculate the distance between to location
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    private double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;
        return dist;
    }
}
