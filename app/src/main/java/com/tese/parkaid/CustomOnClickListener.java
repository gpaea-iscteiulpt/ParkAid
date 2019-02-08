package com.tese.parkaid;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class CustomOnClickListener implements View.OnClickListener {

    private Marker marker;
    private GeoApiContext mGeoApiContext;
    private Location mLocation;
    private GoogleMap mMap;
    private PopupWindow mPopupWindow;
    private ArrayList<PolylineData> mPolylinesData;


    public CustomOnClickListener(Marker marker, GeoApiContext mGeoApiContext, Location mLocation, GoogleMap mMap, PopupWindow mPopupWindow, ArrayList<PolylineData> mPolylinesData) {
        this.marker = marker;
        this.mGeoApiContext = mGeoApiContext;
        this.mLocation = mLocation;
        this.mMap = mMap;
        this.mPopupWindow = mPopupWindow;
        this.mPolylinesData = mPolylinesData;
    }

    @Override
    public void onClick(View v){
        calculateDirections(marker, mGeoApiContext, mLocation);
    }

    public void calculateDirections(Marker marker, GeoApiContext mGeoApiContext, Location mLocation){
        Log.d("Calculate", "calculateDirections: calculating directions.");

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
        Log.d("Calculate", "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d("Calculate", "onResult: routes: " + result.routes[0].toString());
                Log.d("Calculate", "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("Calculate", "onFailure: " + e.getMessage() );

            }
        });

        mPopupWindow.dismiss();
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if(mPolylinesData.size() > 0){
                    mPolylinesData = new ArrayList<>();
                }

                for(DirectionsRoute route: result.routes){
                    List<LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<com.google.android.gms.maps.model.LatLng> newDecodedPath = new ArrayList<>();

                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new com.google.android.gms.maps.model.LatLng(latLng.lat, latLng.lng));
                    }

                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(R.color.grey);
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));
                }
            }
        });
    }

}
