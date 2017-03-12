package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Bhalchandra Wadekar on 28/02/2017.
 */

public class PlaceItem implements ClusterItem, Serializable {

    private String id;
    private PlaceLocation location;
    private String name;
    private SupportedPlaceTypes types;
    private String address;
    private boolean favourite = false;
    private boolean visited = false;
    private String photo = "";
    private HashMap<String, Boolean> photos;

    public HashMap<String, Boolean> getPhotos() {
        return photos;
    }

    public void setPhotos(HashMap<String, Boolean> photos) {
        this.photos = photos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(location.getLat(), location.getLng());
    }

    @Override
    public String getTitle() {
        return getName();
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
        return otherPlaceItem instanceof PlaceItem && this.getId().equals(((PlaceItem) otherPlaceItem).getId());
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SupportedPlaceTypes getTypes() {
        return types;
    }

    public void setTypes(SupportedPlaceTypes types) {
        this.types = types;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public PlaceLocation getLocation() {
        return location;
    }

    public void setLocation(PlaceLocation location) {
        this.location = location;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
