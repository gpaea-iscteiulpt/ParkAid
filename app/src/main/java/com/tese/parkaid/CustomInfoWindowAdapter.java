package com.tese.parkaid;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

public class CustomInfoWindowAdapter implements InfoWindowAdapter{

    private Context mContext;

    public CustomInfoWindowAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.custom_endlocation_marker, null);

        TextView title = view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView duration = view.findViewById(R.id.duration);
        duration.setText(marker.getSnippet());

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
