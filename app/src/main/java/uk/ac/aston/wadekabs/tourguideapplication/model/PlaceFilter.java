package uk.ac.aston.wadekabs.tourguideapplication.model;

import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by Bhalchandra Wadekar on 28/02/2017.
 */

public class PlaceFilter extends Observable {

    private static final int FREE = 0;
    private static final int VERY_EXPENSIVE = 4;

    private static PlaceFilter sInstance;

    private static DatabaseReference sDatabase;

    private int mPriceLevel;
    private Map<String, Boolean> mTypes;

    private PlaceFilter() {

        mPriceLevel = (FREE + VERY_EXPENSIVE) / 2;
        mTypes = new HashMap<>();

        sDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized PlaceFilter getInstance() {

        if (sInstance == null) {

            sInstance = new PlaceFilter();

            sDatabase.child("preferences").child(User.getUser().getUid()).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot placeFilterSnapshot) {

                    sInstance = placeFilterSnapshot.getValue(PlaceFilter.class);

                    PlaceContent.addFilter(sInstance);

                    sInstance.setChanged();
                    sInstance.notifyObservers();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        return sInstance;
    }

    public int getPriceLevel() {
        return mPriceLevel;
    }

    public void setPriceLevel(int cost) {
        if (FREE <= cost && cost <= VERY_EXPENSIVE)
            mPriceLevel = cost;
    }

    public Map<String, Boolean> getTypes() {
        return mTypes;
    }

    public void setTypes(Map<String, Boolean> placeTypes) {
        mTypes = placeTypes;
    }

    @Override
    public String toString() {
        String string = "";
        string += "Price level:\t" + mPriceLevel;

        string += ",\tTypes:\t";
        string += TextUtils.join(", ", mTypes.keySet());

        return string;
    }
}
