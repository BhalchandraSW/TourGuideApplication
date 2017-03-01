package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhalchandra Wadekar on 01/03/2017.
 */

public class PlaceItemContent {

    private static PlaceItemContent sPlaceItemContent;

    private List<PlaceItem> mPlaceItemList = new ArrayList<>();
    private ClusterManager<PlaceItem> mClusterManager;

    public synchronized static PlaceItemContent getInstance() {
        if (sPlaceItemContent == null)
            sPlaceItemContent = new PlaceItemContent();
        return sPlaceItemContent;
    }

    public void addPlaceItem(PlaceItem placeItem) {
        mPlaceItemList.add(placeItem);
        mClusterManager.addItem(placeItem);
    }

    public List<PlaceItem> getPlaceItemList() {
        return mPlaceItemList;
    }
}
