package com.tese.parkaid;

import com.google.android.gms.maps.model.Marker;

public class DecisionFactor {

    public double duration;
    public int occupancy;
    public Marker marker;
    public double distance;
    public double distanceDestinationToPL;
    public double pricePerHour;
    public double weight;
    public String predictedClass;

    public DecisionFactor(double duration, int occupancy, double pricePerHour, double distance, double distanceDestinationToPL, Marker marker, String predictedClass) {
        this.duration = duration;
        this.occupancy = occupancy;
        this.pricePerHour = pricePerHour;
        this.distanceDestinationToPL = distanceDestinationToPL;
        this.distance = distance;
        this.marker = marker;
        this.predictedClass = predictedClass;
    }

    public DecisionFactor() {
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public String getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(String predictedClass) {
        this.predictedClass = predictedClass;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistanceDestinationToPL() {
        return distanceDestinationToPL;
    }

    public void setDistanceDestinationToPL(double distanceDestinationToPL) {
        this.distanceDestinationToPL = distanceDestinationToPL;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(int occupancy) {
        this.occupancy = occupancy;
    }
}

