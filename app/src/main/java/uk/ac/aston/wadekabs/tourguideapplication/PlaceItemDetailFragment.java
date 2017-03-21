package uk.ac.aston.wadekabs.tourguideapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;

/**
 * A fragment representing a single PlaceItem detail screen.
 * This fragment is either contained in a {@link PlaceItemListActivity}
 * in two-pane mode (on tablets) or a {@link NearbyPlaceDetailActivity}
 * on handsets.
 */
public class PlaceItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the position of place item in place item list that this
     * fragment represents.
     */
    public static final String SELECTED_PLACE_ID = "selected_place_id";
    public static final String SELECTED_LIST = "selected_list";

    /**
     * The dummy content this fragment is presenting.
     */
    private Place mPlace;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaceItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(SELECTED_PLACE_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            String placeId = getArguments().getString(SELECTED_PLACE_ID);
            mPlace = PlaceContent.getPlace(placeId);

            Activity activity = this.getActivity();
            activity.setTitle(mPlace.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.nearby_place_detail, container, false);

        TextView address = (TextView) rootView.findViewById(R.id.address);
        address.setText(mPlace.getAddress());

        String[] priceLevels = getResources().getStringArray(R.array.pref_budget_list_titles);

        TextView priceLevel = (TextView) rootView.findViewById(R.id.price_level);
        priceLevel.setText(priceLevels[(int) mPlace.getPriceLevel() + 1]);

        return rootView;
    }
}
