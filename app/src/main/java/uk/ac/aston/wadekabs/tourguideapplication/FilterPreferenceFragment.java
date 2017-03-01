package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;

import java.util.HashSet;

import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceFilter;

/**
 * Created by Bhalchandra Wadekar on 27/02/2017.
 */

public class FilterPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String COST_PREFERENCE = "cost_preference";
    public static final String TYPE_PREFERENCE = "type_preference";

    private ListPreference costPreference;
    private MultiSelectListPreference typePreference;

    private PlaceFilter mFilter = PlaceFilter.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_filter);

        costPreference = (ListPreference) findPreference(COST_PREFERENCE);
        costPreference.setValue(getPreferenceScreen().getSharedPreferences().getString(COST_PREFERENCE, ""));
        int cost = Integer.valueOf(costPreference.getValue());
        String[] costArray = getResources().getStringArray(R.array.pref_budget_list_titles);
        costPreference.setSummary(costArray[cost]);

        typePreference = (MultiSelectListPreference) findPreference(TYPE_PREFERENCE);
        typePreference.setValues(getPreferenceScreen().getSharedPreferences().getStringSet(TYPE_PREFERENCE, new HashSet<String>()));
        // TODO: Set summary for type preference
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

                String[] costArray = getResources().getStringArray(R.array.pref_budget_list_titles);
                costPreference.setSummary(costArray[cost]);

                mFilter.setCost(cost);

                break;

            case TYPE_PREFERENCE:

                // TODO: Set summary for type preference
                mFilter.setPlaceTypes(new HashSet<>(typePreference.getValues()));

                break;
        }
    }
}
