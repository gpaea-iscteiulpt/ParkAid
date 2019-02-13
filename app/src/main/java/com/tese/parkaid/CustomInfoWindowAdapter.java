package com.tese.parkaid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements InfoWindowAdapter {

    private Context mContext;

    public CustomInfoWindowAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.custom_endlocation_marker, null);

        TextView title = view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView duration = view.findViewById(R.id.duration);
        duration.setText(marker.getSnippet());

        return view;

    }
}
