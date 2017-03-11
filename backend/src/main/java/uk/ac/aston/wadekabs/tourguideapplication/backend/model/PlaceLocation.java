package uk.ac.aston.wadekabs.tourguideapplication.backend.model;

/**
 * Created by bhalchandrawadekar on 08/03/2017.
 */

public class PlaceLocation {

    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Location: (" + getLat() + ", " + getLng() + ")";
    }
}
