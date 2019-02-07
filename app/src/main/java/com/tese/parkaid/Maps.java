package com.tese.parkaid;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
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
    private static final String TAG = Maps.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        mParks.add(new Park("Park 1", "Descrição 1", new LatLng(38.740684, -9.227912), "Lisboa", 75, 1, 100, "Seg-Dom", "6:00h-23:00h", R.drawable.parking));
        mParks.add(new Park("Park 2", "Descrição 2", new LatLng(38.734170, -9.223449), "Lisboa", 90, 2, 150, "Seg-Dom", "6:00h-23:00h", R.drawable.parking));
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
        for (Park park : mParks) {

            Bitmap b = BitmapFactory.decodeResource(getResources(), park.getIconPicture());
            Bitmap icon = Bitmap.createScaledBitmap(b, b.getWidth()/4,b.getHeight()/4, false);
            Marker marker = mMap.addMarker(new MarkerOptions().position(park.getLocation()).title(park.getName()).icon(BitmapDescriptorFactory.fromResource(icon)));
            marker.setTag(park);
        }
    }

    private void addMapMarkersCluster(){
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
                MarkerCluster newMarkerCluster = new MarkerCluster(park.getName(),
                        park.getDescription(),
                        park.getIconPicture(),
                        park.getOccupancyPercentage(),
                        park.getPricePerHour(),
                        park.getWorkPeriod(),
                        park.getWorkHours(),
                        park.getLocation());

                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(Maps.this, park));

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
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.custom_map_popup, null);

        Park mPark = (Park) marker.getTag();
        TextView name = (TextView) popupView.findViewById(R.id.name);
        name.setText(mPark.getName());
        TextView description = (TextView) popupView.findViewById(R.id.description);
        description.setText(mPark.getDescription());
        TextView address = (TextView) popupView.findViewById(R.id.address);
        address.setText(mPark.getAddress());
        TextView occupancy = (TextView) popupView.findViewById(R.id.occupancy);
        occupancy.setText(mPark.getOccupancyPercentage() + "%");
        ImageView photo = (ImageView) popupView.findViewById(R.id.photo);
        photo.setImageResource(mPark.getPhoto());
        TextView hours = (TextView) popupView.findViewById(R.id.hours);
        hours.setText(mPark.getWorkHours());
        TextView price = (TextView) popupView.findViewById(R.id.price);
        String price_string = String.valueOf(mPark.getPricePerHour());
        //price.setText(price_string + "");
        TextView period = (TextView) popupView.findViewById(R.id.period);
        //period.setText(mPark.getWorkPeriod());
        TextView slots = (TextView) popupView.findViewById(R.id.slots);
        //slots.setText(mPark.getTotalSlots());

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }

        return false;
    }
}
