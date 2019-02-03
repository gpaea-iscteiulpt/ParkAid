package com.tese.parkaid;

import android.media.Image;

import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerCluster implements ClusterItem{

    private String name;
    private String description;
    private int iconPicture;
    private int occupancyPercentage;
    private double pricePerHour;
    private String workPeriod;
    private String workHours;
    private LatLng location;


    public MarkerCluster() {
    }

    public MarkerCluster(String name, String description, int iconPicture, int occupancyPercentage, double pricePerHour, String workPeriod, String workHours, LatLng location) {
        this.name = name;
        this.description = description;
        this.iconPicture = iconPicture;
        this.occupancyPercentage = occupancyPercentage;
        this.pricePerHour = pricePerHour;
        this.workPeriod = workPeriod;
        this.workHours = workHours;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIconPicture() {
        return iconPicture;
    }

    public void setIconPicture(int iconPicture) {
        this.iconPicture = iconPicture;
    }

    public int getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(int occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getWorkPeriod() {
        return workPeriod;
    }

    public void setWorkPeriod(String workPeriod) {
        this.workPeriod = workPeriod;
    }

    public String getWorkHours() {
        return workHours;
    }

    public void setWorkHours(String workHours) {
        this.workHours = workHours;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Override
    public LatLng getPosition() {
        return location;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
