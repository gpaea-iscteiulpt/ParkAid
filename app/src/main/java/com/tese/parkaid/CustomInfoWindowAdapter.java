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
        Park p = (Park) marker.getTag();
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(mPark.getName());
        TextView description = (TextView) view.findViewById(R.id.description);
        description.setText(mPark.getDescription());
        TextView address = (TextView) view.findViewById(R.id.address);
        address.setText(mPark.getAddress());
        TextView occupancy = (TextView) view.findViewById(R.id.occupancy);
        occupancy.setText(mPark.getOccupancyPercentage() + "%");
        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        photo.setImageResource(mPark.getPhoto());
        TextView hours = (TextView) view.findViewById(R.id.hours);
        hours.setText(mPark.getWorkHours());
        TextView price = (TextView) view.findViewById(R.id.price);
        String price_string = String.valueOf(mPark.getPricePerHour());
        //price.setText(price_string + "");
        TextView period = (TextView) view.findViewById(R.id.period);
        //period.setText(mPark.getWorkPeriod());
        TextView slots = (TextView) view.findViewById(R.id.slots);
        //slots.setText(mPark.getTotalSlots());
    }

    @Override
    public View getInfoWindow(Marker marker) {
        fillPopWindow(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        fillPopWindow(marker, mWindow);
        return mWindow;
    }
}
