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

public class PlaceContent extends Observable implements Observer {

    private static final String FAVOURITES = "favourites";
    private static final String NEARBY = "nearby";

    private static PlaceContent sFavourites;
    private static PlaceContent sNearby;

    private static DatabaseReference sDatabase;

    private String mType;
    private List<Place> mPlaceList;
    private PlaceChildEventListener mListener;

    private PlaceContent(String type) {
        mType = type;
        sDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static List<Place> favourites() {
        if (sFavourites == null) {
            sFavourites = new PlaceContent(FAVOURITES);
        }
        return sFavourites.getPlaceList();
    }

    public static List<Place> nearby() {
        if (sNearby == null) {
            sNearby = new PlaceContent(NEARBY);

            PlaceFilter instance = PlaceFilter.getInstance();
            instance.addObserver(sNearby);
        }
        return sNearby.getPlaceList();
    }

    public static void addFavourite(Place place) {
        sDatabase.child(FAVOURITES).child(place.getPlaceId()).setValue(true);
    }

    public static void addFavouritesObserver(Observer observer) {
        sFavourites.addObserver(observer);
    }

    public static void addNearbyObserver(Observer observer) {
        sNearby.addObserver(observer);
    }

    public static void addFilter(PlaceFilter filter) {
        filter.addObserver(sNearby);
    }

    private List<Place> getPlaceList() {

        if (mPlaceList == null) {
            mPlaceList = new ArrayList<>();
            mListener = new PlaceChildEventListener(mPlaceList);
            sDatabase.child(mType).child(User.getUser().getUid()).addChildEventListener(mListener);
        }

        return mPlaceList;
    }

    public static Place getPlace(String placeId, String list) {
        switch (list) {
            case FAVOURITES:
                for (Place place : favourites()) {
                    if (place.getPlaceId().equals(placeId)) {
                        return place;
                    }
                }
                break;
            case NEARBY:
                for (Place place : nearby()) {
                    if (place.getPlaceId().equals(placeId)) {
                        return place;
                    }
                }
                break;
        }
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (mPlaceList.size() > 0) {
            mPlaceList.clear();
            setChanged();
            notifyObservers();
        }

        sDatabase.child(mType).child(User.getUser().getUid()).removeEventListener(mListener);
        sDatabase.child(mType).child(User.getUser().getUid()).addChildEventListener(mListener);
    }

    private class PlaceChildEventListener implements ChildEventListener {

        private List<Place> mPlaceList;

        PlaceChildEventListener(List<Place> placeList) {
            mPlaceList = placeList;
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

                    Place place = placeDetailsSnapshot.getValue(Place.class);

                    System.out.println(place);

                    if (place != null && place.satisfiesFilter()) {
                        place.setPlaceId(placeId);

                        // TODO: Remove listeners instead of this workaround
                        if (!mPlaceList.contains(place)) {
                            mPlaceList.add(place);
                            setChanged();
                            notifyObservers();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        private void removePlaceHavingPlaceId(String placeId) {

            mPlaceList.remove(new Place(placeId));

            setChanged();
            notifyObservers();
        }
    }
}
