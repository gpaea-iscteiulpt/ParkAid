package com.tese.parkaid;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

public class Maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LatLngBounds mMapBoundary;
    private Location mLocation;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mMyClusterManagerRenderer;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<MarkerCluster> mClusterMarkers = new ArrayList<>();
    private ArrayList<Park> mParks = new ArrayList<>();
    private static final String TAG = Map.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocation = (Location) getIntent().getParcelableExtra("LastLocation");

        addMapMarkers();
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        startLocationService();
        setCameraView();
    }

    private void fillParks() {
        mParks.add(new Park("Park 1", "Descrição 1", new LatLng(37.423027, -122.086226), 75, 1, 100, "Seg-Dom", "6:00h-23:00h"));
        mParks.add(new Park("Park 2", "Descrição 2", new LatLng(37.420445, -122.084995), 90, 2, 150, "Seg-Dom", "6:00h-23:00h"));
    }

    private void setCameraView() {

        double bottomBoundary = mLocation.getLatitude() - .1;
        double leftBoundary = mLocation.getLongitude() - .1;
        double topBoundary = mLocation.getLatitude() + .1;
        double rightBoundary = mLocation.getLongitude() + .1;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }



    private void addMapMarkers(){
        fillParks();
        if(mMap != null) {
            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<MarkerCluster>(this.getApplicationContext(), mMap);
            }
            if (mMyClusterManagerRenderer == null) {
                mMyClusterManagerRenderer = new MyClusterManagerRenderer(this, mMap, mClusterManager);
                mClusterManager.setRenderer(mMyClusterManagerRenderer);
            }
            for (Park park : mParks) {
                MarkerCluster newMarkerCluster = new MarkerCluster(park.getLocation(), park.getName(), park.getName(), park.getIconPicture());
                mClusterManager.addItem(newMarkerCluster);
                mClusterMarkers.add(newMarkerCluster);
            }
            mClusterManager.cluster();
        }

    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Maps.this.startForegroundService(serviceIntent);
            } else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.".equals(service.service.getClassName())){
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(Maps.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }
}
