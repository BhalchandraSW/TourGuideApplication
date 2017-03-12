package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Bhalchandra Wadekar on 12/03/2017.
 */

public class PlaceItemContent extends Observable {

    private static final String FAVOURITES = "favourites";
    private static final String NEARBY = "nearby";

    private static PlaceItemContent sFavourites;
    private static PlaceItemContent sNearby;
    private static DatabaseReference sDatabase;

    private String mType;
    private List<PlaceItem> mPlaceItemList;

    private PlaceItemContent(String type) {
        mType = type;
        sDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static List<PlaceItem> favourites() {
        if (sFavourites == null) {
            sFavourites = new PlaceItemContent(FAVOURITES);
        }
        return sFavourites.getPlaceItemList();
    }

    public static List<PlaceItem> nearby() {
        if (sNearby == null) {
            sNearby = new PlaceItemContent(NEARBY);
        }
        return sNearby.getPlaceItemList();
    }

    public static void addFavourite(PlaceItem placeItem) {
        sDatabase.child(FAVOURITES).child(placeItem.getId()).setValue(true);
    }

    public static void addFavouritesObserver(Observer observer) {
        sFavourites.addObserver(observer);
    }

    public static void addNearbyObserver(Observer observer) {
        sNearby.addObserver(observer);
    }

    private List<PlaceItem> getPlaceItemList() {

        if (mPlaceItemList == null) {
            mPlaceItemList = new ArrayList<>();
            sDatabase.child(mType).child(User.getUser().getUid()).addChildEventListener(new PlaceItemChildEventListener(mPlaceItemList));
        }

        return mPlaceItemList;
    }

    private class PlaceItemChildEventListener implements ChildEventListener {

        private List<PlaceItem> mPlaceItemList;

        PlaceItemChildEventListener(List<PlaceItem> placeItemList) {
            mPlaceItemList = placeItemList;
        }

        @Override
        public void onChildAdded(DataSnapshot placeIdSnapshot, String s) {
            addPlaceHavingPlaceId(placeIdSnapshot.getKey());
        }

        @Override
        public void onChildChanged(DataSnapshot placeIdSnapshot, String s) {
            String placeId = placeIdSnapshot.getKey();
            removePlaceHavingPlaceId(placeId);
            addPlaceHavingPlaceId(placeId);
        }

        @Override
        public void onChildRemoved(DataSnapshot placeIdSnapshot) {
            removePlaceHavingPlaceId(placeIdSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

        private void addPlaceHavingPlaceId(final String placeId) {

            sDatabase.child("details").child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot placeDetailsSnapshot) {

                    PlaceItem placeItem = placeDetailsSnapshot.getValue(PlaceItem.class);

                    if (placeItem != null) {
                        placeItem.setId(placeId);
                        mPlaceItemList.add(placeItem);
                        setChanged();
                        notifyObservers();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }

        private void removePlaceHavingPlaceId(String placeId) {

            PlaceItem placeToBeRemoved = new PlaceItem();
            placeToBeRemoved.setId(placeId);
            mPlaceItemList.remove(placeToBeRemoved);

            setChanged();
            notifyObservers();
        }
    }
}
