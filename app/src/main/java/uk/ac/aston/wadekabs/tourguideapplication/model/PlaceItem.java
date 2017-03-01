package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

/**
 * Created by Bhalchandra Wadekar on 28/02/2017.
 */

public class PlaceItem implements ClusterItem, Serializable {

    private String id;
    private double lat, lng;
    private String title;
    private String address;
    private boolean favourite = false;
    private boolean visited = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(lat, lng);
    }

    public void setPosition(LatLng position) {
        this.lat = position.latitude;
        this.lng = position.longitude;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return getAddress();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object otherPlaceItem) {
        return otherPlaceItem instanceof PlaceItem && getId().equals(((PlaceItem) otherPlaceItem).getId());
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
