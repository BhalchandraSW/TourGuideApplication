package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.aston.wadekabs.tourguideapplication.model.User;

/**
 * Created by Bhalchandra Wadekar on 27/02/2017.
 */

public class FilterPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String COST_PREFERENCE = "cost_preference";
    public static final String TYPE_PREFERENCE = "type_preference";

    private ListPreference costPreference;
    private MultiSelectListPreference typePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_filter);

        costPreference = (ListPreference) findPreference(COST_PREFERENCE);
//        costPreference.setValue(getPreferenceScreen().getSharedPreferences().getString(COST_PREFERENCE, ""));
//        int cost = Integer.valueOf(costPreference.getValue());
        final String[] costArray = getResources().getStringArray(R.array.pref_budget_list_titles);
//        costPreference.setSummary(costArray[cost]);

        typePreference = (MultiSelectListPreference) findPreference(TYPE_PREFERENCE);
        typePreference.setValues(getPreferenceScreen().getSharedPreferences().getStringSet(TYPE_PREFERENCE, new HashSet<String>()));
        // TODO: Set summary for type preference

        if (User.getUser() != null) {
            FirebaseDatabase.getInstance().getReference("preferences").child(User.getUser().getUid()).child("priceLevel").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot costSnapshot) {
                    int cost = costSnapshot.getValue() != null ? Integer.valueOf(costSnapshot.getValue().toString()) : 2;
                    costPreference.setValue(String.valueOf(cost));
                    costPreference.setSummary(costArray[cost]);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (User.getUser() != null) {
            FirebaseDatabase.getInstance().getReference("preferences").child(User.getUser().getUid()).child("types").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    List<String> selectedTypes = new ArrayList<>();

                    for (DataSnapshot typeSnapshot : dataSnapshot.getChildren()) {
                        String type = typeSnapshot.getKey();
                        type = type.replace('_', ' ');
                        type = StringUtils.capitaliseAllWords(type);
                        selectedTypes.add(type);
                    }

                    typePreference.setSummary(TextUtils.join(", ", selectedTypes));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch (key) {

            case COST_PREFERENCE:

                int cost = Integer.valueOf(costPreference.getValue());
                FirebaseDatabase.getInstance().getReference("preferences").child(User.getUser().getUid()).child("priceLevel").setValue(cost);

                break;

            case TYPE_PREFERENCE:

                Set<String> selectedTypes = typePreference.getValues();

                for (CharSequence type : typePreference.getEntryValues()) {
                    if (selectedTypes.contains(type.toString())) {
                        FirebaseDatabase.getInstance().getReference("preferences").child(User.getUser().getUid()).child("types").child(type.toString()).setValue(true);
                    } else {
                        FirebaseDatabase.getInstance().getReference("preferences").child(User.getUser().getUid()).child("types").child(type.toString()).removeValue();
                    }
                }

                break;
        }
    }
}
