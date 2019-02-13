package com.tese.parkaid;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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
    public void onClick(View v) {
        //calculateDirections(marker);
    }
}
