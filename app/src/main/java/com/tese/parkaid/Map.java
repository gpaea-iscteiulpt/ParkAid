package com.tese.parkaid;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

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


public class Map extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary;
    private Location mLocation;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mMyClusterManagerRenderer;
    private ArrayList<MarkerCluster> mClusterMarkers = new ArrayList<>();
    private ArrayList<Park> mParks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fillParks();

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void fillParks(){
        mParks.add(new Park("Park1", "Descrição 1", new LatLng(38.751249, -9.155369),75, 1, 100, "Seg-Dom", "6:00h-23:00h"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if(getIntent().getExtras() != null){
            mLocation = (Location) getIntent().getSerializableExtra("LastLocation");
        }

        //LatLng lisbon = new LatLng(38.722185, -9.139276);

        //mPark1 = mGoogleMap.addMarker(new MarkerOptions().position(park1).title("Park1").icon(BitmapDescriptorFactory.fromResource(R.drawable.parkingfree)));
        //mPark1.setTag(0);
        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lisbon, 10));
        //mGoogleMap.setOnMarkerClickListener(this);

        setCameraView();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void setCameraView(){
        double bottomBoundary = mLocation.getLatitude() - .1;
        double leftBoundary = mLocation.getLongitude() - .1;
        double topBoundary = mLocation.getLatitude() + .1;
        double rightBoundary = mLocation.getLongitude() + .1;

        mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary,rightBoundary));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    private void addMapMarkers(){
        if(mGoogleMap != null) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<MarkerCluster>(this.getApplicationContext(), mGoogleMap);
            }
            if (mMyClusterManagerRenderer == null) {
                mMyClusterManagerRenderer = new MyClusterManagerRenderer(this, mGoogleMap, mClusterManager);
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
}
