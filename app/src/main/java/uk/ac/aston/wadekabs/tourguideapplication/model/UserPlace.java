package uk.ac.aston.wadekabs.tourguideapplication.model;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Map;

/**
 * Created by Bhalchandra Wadekar on 22/03/2017.
 */

public class UserPlace {

    public static GoogleApiClient sGoogleApiClient;

    com.google.android.gms.location.places.Place mPlace;

    private boolean mFavourite = false;
    private Date mVisitDate;
    private Map<String, Boolean> mPhotos;
    private String mNote;

    public UserPlace(final String placeId) {

        Places.GeoDataApi.getPlaceById(sGoogleApiClient, placeId).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                    mPlace = places.get(0);
                }
                places.release();
            }
        });

        User.getInstance().getFirebaseReference()
                .child("favourites")
                .child(placeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mFavourite = true;
                        mVisitDate = (Date) dataSnapshot.child("visitDate").getValue();
                        mPhotos = (Map<String, Boolean>) dataSnapshot.child("photos").getValue();
                        mNote = (String) dataSnapshot.child("note").getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public com.google.android.gms.location.places.Place getPlace() {
        return mPlace;
    }
}
