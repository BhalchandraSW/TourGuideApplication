package uk.ac.aston.wadekabs.tourguideapplication.model;

import android.graphics.Bitmap;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

/**
 * Created by Bhalchandra Wadekar on 13/03/2017.
 */

public class Place extends Observable {

    private String mPlaceId;
    private PlaceLocation mLocation;
    private String mName;
    private String mAddress;
    private long mPriceLevel = -1; // -1 = unknown price level
    private Map<String, Boolean> mTypes;
    private Map<String, Boolean> mPicturesMap;
    private List<String> mPictures;
    private String notes;

    private List<Bitmap> mPhotos;

    private boolean mFavourite;
    private boolean mVisited;

    private Date mWantToVisitDate;

    public Place() {
    }

    public Place(String placeId) {
        this.mPlaceId = placeId;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    void setPlaceId(String placeId) {
        this.mPlaceId = placeId;
    }

    public PlaceLocation getLocation() {
        return mLocation;
    }

    public void setLocation(PlaceLocation location) {
        this.mLocation = location;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public boolean isFavourite() {
        return mFavourite;
    }

    public void setFavourite(boolean favourite) {
        this.mFavourite = favourite;
    }

    public boolean isVisited() {
        return mVisited;
    }

    public void setVisited(boolean visited) {
        this.mVisited = visited;
    }

    public long getPriceLevel() {
        return mPriceLevel;
    }

    public void setPriceLevel(long priceLevel) {
        this.mPriceLevel = priceLevel;
    }

    public Map<String, Boolean> getTypes() {
        return mTypes;
    }

    public void setTypes(Map<String, Boolean> types) {
        this.mTypes = types;
    }

    public Date getWantToVisitDate() {
        return mWantToVisitDate;
    }

    public void setWantToVisitDate(Date wantToVisitDate) {
        this.mWantToVisitDate = wantToVisitDate;
        FirebaseDatabase.getInstance().getReference("users").child(User.getInstance().getUser().getUid()).child("favourites").child(mPlaceId).child("visitDate").setValue(wantToVisitDate);
    }

    public List<Bitmap> getPhotos() {
        if (mPhotos == null)
            mPhotos = new ArrayList<>();
        return mPhotos;
    }

    public void setPhotos(List<Bitmap> photos) {
        this.mPhotos = photos;
    }

    public void addPhoto(Bitmap photo) {
        getPhotos().add(photo);
        setChanged();
        notifyObservers();
    }

    public List<String> getPictures() {
        return mPictures;
    }

    public void setPictures(Map<String, Boolean> picturesMap) {
        mPicturesMap = picturesMap;
        this.mPictures = new ArrayList<>(mPicturesMap.keySet());
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;

        FirebaseDatabase.getInstance().getReference("users").child(User.getInstance().getUser().getUid()).child("favourites").child(mPlaceId).child("notes").setValue(notes);
    }

    boolean satisfiesFilter() {

        if (mPriceLevel == -1 || mPriceLevel == PlaceFilter.getInstance().getPriceLevel()) {
            Set<String> allowedTypes = PlaceFilter.getInstance().getTypes().keySet();
            if (allowedTypes.size() > 0) {
                for (String type : allowedTypes) {
                    if (mTypes.containsKey(type))
                        return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Place && mPlaceId.equals(((Place) obj).mPlaceId));
    }

    @Override
    public String toString() {
        return mName;
    }
}
