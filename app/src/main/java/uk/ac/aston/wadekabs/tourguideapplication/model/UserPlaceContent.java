package uk.ac.aston.wadekabs.tourguideapplication.model;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bhalchandra Wadekar on 22/03/2017.
 */

public class UserPlaceContent {

    private static UserPlaceContent sInstance;

    private UserPlaceFilter mFilter;

    private List<UserPlace> mFavourites = new ArrayList<>();
    private List<UserPlace> mNearby = new ArrayList<>();

    private static final DatabaseReference sDatabase = FirebaseDatabase.getInstance().getReference();

    private UserPlaceContent() {

        User.getInstance().getFirebaseReference()
                .child("devices")
                .child(FirebaseInstanceId.getInstance().getId())
                .child("nearby")
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        mNearby.add(new UserPlace(dataSnapshot.getKey()));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        // mNearby.remove(new UserPlace(dataSnapshot.getKey()));
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        User.getInstance().getFirebaseReference()
                .child("favourites")
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        mFavourites.add(new UserPlace(dataSnapshot.getKey()));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public static UserPlaceContent getInstance() {
        if (sInstance == null)
            sInstance = new UserPlaceContent();
        return sInstance;
    }

    public List<UserPlace> getFavourites() {
        return mFavourites;
    }

    public List<UserPlace> getNearby() {
        return mNearby;
    }

    public UserPlace getPlaceWithId(String placeId) {

        for (UserPlace userPlace : mFavourites) {
            if (userPlace.getPlace().getId().equals(placeId))
                return userPlace;
        }

        for (UserPlace userPlace : mNearby) {
            if (userPlace.getPlace().getId().equals(placeId))
                return userPlace;
        }

        return null;
    }
}
