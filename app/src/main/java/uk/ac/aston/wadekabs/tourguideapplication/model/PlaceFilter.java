package uk.ac.aston.wadekabs.tourguideapplication.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Bhalchandra Wadekar on 28/02/2017.
 */

public class PlaceFilter {

    private static final int MOST_AFFORDABLE = 0;
    private static final int MOST_EXPENSIVE = 4;

    private static PlaceFilter mInstance;
    private int mCost;
    private Set<String> mPlaceTypes = new HashSet<>();

    private PlaceFilter() {
        mCost = (MOST_AFFORDABLE + MOST_EXPENSIVE) / 2;
        mPlaceTypes = new HashSet<>();
        mPlaceTypes.add("art_gallery");
        mPlaceTypes.add("museum");
        mPlaceTypes.add("restaurant");
        // mPlaceTypes.add("parking");
    }

    public static synchronized PlaceFilter getInstance() {
        if (mInstance == null) {
            mInstance = new PlaceFilter();
        }
        return mInstance;
    }

    public int getCost() {
        return mCost;
    }

    public void setCost(int cost) {
        if (MOST_AFFORDABLE <= cost && cost <= MOST_EXPENSIVE)
            mCost = cost;
    }

    public Set<String> getPlaceTypes() {
        return mPlaceTypes;
    }

    public void setPlaceTypes(Set<String> placeTypes) {
        mPlaceTypes = placeTypes;
    }
}
