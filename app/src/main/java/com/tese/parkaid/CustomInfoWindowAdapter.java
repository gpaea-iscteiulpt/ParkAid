package com.tese.parkaid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

public class CustomInfoWindowAdapter implements InfoWindowAdapter{

    private final View mWindow;
    private Context mContext;
    private Park mPark;

    public CustomInfoWindowAdapter(Context mContext, Park mPark) {
        this.mContext = mContext;
        this.mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_map_popup, null);
        this.mPark = mPark;
    }

    private void fillPopWindow(Marker marker, View view){
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(mPark.getName());
        TextView description = (TextView) view.findViewById(R.id.description);
        description.setText(mPark.getDescription());
        TextView address = (TextView) view.findViewById(R.id.address);
        TextView occupancy = (TextView) view.findViewById(R.id.occupancy);
        ImageView photo = (ImageView) view.findViewById(R.id.photo);


    }

    @Override
    public View getInfoWindow(Marker marker) {

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
