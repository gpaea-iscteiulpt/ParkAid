package com.tese.parkaid;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class Maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnInfoWindowClickListener{

    private GoogleMap mMap;
    private LatLngBounds mMapBoundary;
    private Location mLocation;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mMyClusterManagerRenderer;
    private ArrayList<MarkerCluster> mClusterMarkers = new ArrayList<>();
    private ArrayList<Park> mParks = new ArrayList<>();
    private static final String TAG = Maps.class.getSimpleName();
    private GeoApiContext mGeoApiContext = null;
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private Marker mMarkerSelected = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private String mWhereFrom;
    private static final float DEFAULT_ZOOM = 15f;
    private Address mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btReset = (Button) findViewById(R.id.btReset);
        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMap();
                addMapMarkers();
            }
        });

        if(mGeoApiContext == null){
            mGeoApiContext =  new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocation = (Location) getIntent().getParcelableExtra("LastLocation");
        mWhereFrom = (String) getIntent().getStringExtra("WhereFrom");

        addMapMarkers();
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(this);
        startLocationService();

        switch (mWhereFrom){
            case "FromMap":
                setCameraView(mLocation);
                break;
            case "FromSearch":
                mAddress = (Address) getIntent().getParcelableExtra("Address");
                Location temp = new Location(LocationManager.GPS_PROVIDER);
                temp.setLatitude(mAddress.getLatitude());
                temp.setLongitude(mAddress.getLongitude());
                setCameraView(temp);
                break;
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        hideSoftKeyboard();

    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void fillParks() {
        mParks.add(new Park("Park 1", "Descrição 1", new LatLng(38.740684, -9.227912), "Lisboa", 75, 1, 100, "Seg-Dom", "6:00h-23:00h", R.drawable.parking));
        mParks.add(new Park("Park 2", "Descrição 2", new LatLng(38.734170, -9.223449), "Lisboa", 90, 2, 150, "Seg-Dom", "6:00h-23:00h", R.drawable.parking));
    }

    private void setCameraView(Location loc) {

        double bottomBoundary = loc.getLatitude() - .1;
        double leftBoundary = loc.getLongitude() - .1;
        double topBoundary = loc.getLatitude() + .1;
        double rightBoundary = loc.getLongitude() + .1;

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
            Bitmap icon = Bitmap.createScaledBitmap(b, b.getWidth()/14,b.getHeight()/14, false);
            Marker marker = mMap.addMarker(new MarkerOptions().position(park.getLocation()).title(park.getName()).icon(BitmapDescriptorFactory.fromBitmap(icon)));
            marker.setTag(park);
        }
        mMap.setOnInfoWindowClickListener(this);
    }

//    private void addMapMarkersCluster(){
//        fillParks();
//        if(mMap != null) {
//            if (mClusterManager == null) {
//                mClusterManager = new ClusterManager<MarkerCluster>(this.getApplicationContext(), mMap);
//            }
//            if (mMyClusterManagerRenderer == null) {
//                mMyClusterManagerRenderer = new MyClusterManagerRenderer(this, mMap, mClusterManager);
//                mClusterManager.setRenderer(mMyClusterManagerRenderer);
//            }
//            for (Park park : mParks) {
//                MarkerCluster newMarkerCluster = new MarkerCluster(park.getName(),
//                        park.getDescription(),
//                        park.getIconPicture(),
//                        park.getOccupancyPercentage(),
//                        park.getPricePerHour(),
//                        park.getWorkPeriod(),
//                        park.getWorkHours(),
//                        park.getLocation());
//
//                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(Maps.this, park));
//
//                mClusterManager.addItem(newMarkerCluster);
//                mClusterMarkers.add(newMarkerCluster);
//            }
//            mClusterManager.cluster();
//        }
//
//    }

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

    private void removeTripMakers(){
        for(Marker marker: mTripMarkers){
            marker.remove();
        }
    }

    private void resetSelectedMarker(){
        if(mMarkerSelected != null) {
            mMarkerSelected.setVisible(true);
            mMarkerSelected = null;
            removeTripMakers();
        }
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.custom_map_popup, null);

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
        TextView period = (TextView) popupView.findViewById(R.id.period);
        period.setText(mPark.getWorkPeriod());
        TextView price = (TextView) popupView.findViewById(R.id.price);
        price.setText(mPark.getPricePerHour() + "€ p/h");

        Button go = (Button) popupView.findViewById(R.id.go);
        String tempString = "Go to location";
        SpannableString spanString = new SpannableString(tempString);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        go.setText(spanString);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSelectedMarker();
                mMarkerSelected = marker;
                calculateDirections(marker);
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        TextView close = (TextView) popupView.findViewById(R.id.close);
        close.setOnTouchListener(new View.OnTouchListener() {
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

    public void calculateDirections(Marker marker){
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLocation.getLatitude(),
                        mLocation.getLongitude()
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("Calculate", "onFailure: " + e.getMessage() );
            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if(mPolylinesData.size() > 0){
                    for(PolylineData polylineData: mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                }

                double duration = 99999999;
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<com.google.android.gms.maps.model.LatLng> newDecodedPath = new ArrayList<>();

                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new com.google.android.gms.maps.model.LatLng(latLng.lat, latLng.lng));
                    }

                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));

                    polyline.setClickable(true);
                    polyline.setColor(ContextCompat.getColor(Maps.this, R.color.grey));
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));

                    mMarkerSelected.setVisible(false);

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }
                }
            }
        });
    }

    private void resetMap(){
        if(mMap != null) {
            mMap.clear();

            if(mClusterManager != null){
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if(mPolylinesData.size() > 0){
                mPolylinesData.clear();
                mPolylinesData = new ArrayList<>();
            }
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolylinesData){
            index++;
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.lightblue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(polylineData.getLeg().endLocation.lat, polylineData.getLeg().endLocation.lng);
                Marker marker = mMap.addMarker(new MarkerOptions().position(endLocation)
                .title("Trip: #" + index)
                .snippet("Duration: " + polylineData.getLeg().duration));

                marker.showInfoWindow();

                mTripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.grey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    @Override
    public void onInfoWindowClick(final Marker markerSelected) {
        if(markerSelected.getTitle().contains("Trip: #")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(markerSelected.getPosition().latitude);
                            String longitude = String.valueOf(markerSelected.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try{
                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }catch (NullPointerException e){
                                Toast.makeText(Maps.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

}