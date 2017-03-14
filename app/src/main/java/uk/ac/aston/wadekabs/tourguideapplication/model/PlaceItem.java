package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Bhalchandra Wadekar on 28/02/2017.
 */

public class PlaceItem implements ClusterItem {

    private Place place;

    public PlaceItem(Place place) {
        this.place = place;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public LatLng getPosition() {
        return place.getLocation().latLng();
    }

    @Override
    public String getTitle() {
        return place.getName();
    }

    @Override
    public String getSnippet() {
        return place.getAddress();
    }
}
