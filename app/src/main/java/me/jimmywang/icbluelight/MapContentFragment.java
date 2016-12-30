package me.jimmywang.icbluelight;

/**
 * This is the overall monitor map view used under staff role. It will show all student help request and update their
 * location in real time.
 *
 * Created by yanmingwang on 12/10/16.
 */

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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Map;
import static me.jimmywang.icbluelight.LoginActivity.PREFS_NAME;

public class MapContentFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {
    private HelpUpdateFragment.onSafe callback;
    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean firstupdate;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private SharedPreferences settings;
    private GoogleMap mMap;
    private View view;
    private float MarkerColor;

    private static final LatLng IC = new LatLng(42.422121, -76.494127);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(IC));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //check if staff want to show self location
        if(settings.getBoolean("ShowMyLocation",true)){
            enableMyLocation();
        }else{
            mMap.setMyLocationEnabled(false);
        }
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
     * Create location request
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
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

    public MapContentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */

        settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        view = inflater.inflate(R.layout.fragment_maps, container, false);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Set marker color base on setting
        switch (settings.getString("MarkerColor","Red")){
            case "Red":
                MarkerColor = BitmapDescriptorFactory.HUE_RED;
                break;
            case "Blue":
                MarkerColor = BitmapDescriptorFactory.HUE_BLUE;
                break;
            default:
                MarkerColor = BitmapDescriptorFactory.HUE_RED;
                break;
        }


        createLocationRequest();
        firstupdate = true;

        /**
         * Set database listener, update map everytime if an event is added removed or changed
         */
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            //get data as Map
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateMap((Map<String, Object>) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    /**
     * This method update the map and all the events on it
     * @param events an object with all events in the database
     */
    private void updateMap(Map<String, Object> events){
        ArrayList<Map> temp = new ArrayList<>();

        //check if everything is really
        if(mMap == null){
            return;
        }else if(events==null){
            mMap.clear();
            return;
        }

        //Add all event in a list
        for(Map.Entry<String, Object> event : events.entrySet()){
            Map singleEvent = (Map) event.getValue();
            temp.add(singleEvent);
        }

        if(mMap != null){
            mMap.clear();
        }

        //read all events and redraw markers base on new data
        for (int i = 0; i < temp.size(); i++) {
            String longitude = (String) temp.get(i).get("longitude");
            String latitude = (String)temp.get(i).get("latitude");
            String username = (String)temp.get(i).get("username");
            String detailLocation = (String)temp.get(i).get("detailLocation");
            String additionMessage = (String)temp.get(i).get("additionMessage");
            String timestemp= (String)temp.get(i).get("timestemp");
            LatLng tempL = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            mMap.addMarker(new MarkerOptions()
                    .position(tempL)
                    .title(username)
                    .snippet("Detail Location: "+ detailLocation + " Addition Message: " +additionMessage+ " Time Stemp: " + timestemp)
                    .icon(BitmapDescriptorFactory.defaultMarker(MarkerColor))
            );
        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroyView() {

        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            getActivity().getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
        super.onDestroyView();
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

}
