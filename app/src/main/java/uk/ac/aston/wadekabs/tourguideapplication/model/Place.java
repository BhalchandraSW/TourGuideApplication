package uk.ac.aston.wadekabs.tourguideapplication.model;

import java.util.Map;
import java.util.Set;

/**
 * Created by Bhalchandra Wadekar on 13/03/2017.
 */

public class Place {

    private String mPlaceId;
    private PlaceLocation mLocation;
    private String mName;
    private String mAddress;
    private long mPriceLevel = -1; // -1 = unknown price level
    private Map<String, Boolean> mTypes;

    private boolean mFavourite;
    private boolean mVisited;

    public Place() {
    }

    public Place(String placeId) {
        this.mPlaceId = placeId;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public void setPlaceId(String placeId) {
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

    public boolean satisfiesFilter() {

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
