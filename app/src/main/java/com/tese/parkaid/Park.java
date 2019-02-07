package com.tese.parkaid;

import com.google.android.gms.maps.model.LatLng;

public class Park {

    private String name;
    private String description;
    private int iconPicture;
    private int occupancyPercentage;
    private double pricePerHour;
    private int totalSlots;
    private String workPeriod;
    private String workHours;
    private LatLng location;
    private String address;
    private int photo;

    public Park(String name, String description, LatLng location, String address, int occupancyPercentage, double pricePerHour, int totalSlots, String workPeriod, String workHours, int photo) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.address = address;
        this.photo = photo;
        if(occupancyPercentage >= 85){
            iconPicture = R.drawable.parkingfull;
        }else if(occupancyPercentage < 85 & occupancyPercentage >= 50){
            iconPicture = R.drawable.parkinghalf;
        }else{
            iconPicture = R.drawable.parkingfree;
        }
        this.occupancyPercentage = occupancyPercentage;
        this.pricePerHour = pricePerHour;
        this.totalSlots = totalSlots;
        this.workPeriod = workPeriod;
        this.workHours = workHours;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
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

}
