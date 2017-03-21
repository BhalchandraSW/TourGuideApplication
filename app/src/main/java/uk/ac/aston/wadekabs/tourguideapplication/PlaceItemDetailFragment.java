package uk.ac.aston.wadekabs.tourguideapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
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
 * in two-pane mode (on tablets) or a {@link PlaceItemDetailActivity}
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
            String list = getArguments().getString(SELECTED_LIST);
            mPlace = PlaceContent.getPlace(placeId, list);

            Activity activity = this.getActivity();

            AppBarLayout appBarLayout = (AppBarLayout) activity.findViewById(R.id.app_bar);
            activity.setTitle(mPlace.getName());
//            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//            if (appBarLayout != null) {
//                appBarLayout.setTitle(mPlaceItem.getName());
//            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.nearby_place_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mPlace != null) {

            ((TextView) rootView.findViewById(R.id.address)).setText(mPlace.getAddress());

            TextView priceLevel = (TextView) rootView.findViewById(R.id.price_level);

            String[] priceLevels = getResources().getStringArray(R.array.pref_budget_list_titles);
            if (mPlace.getPriceLevel() > 0) {
                priceLevel.setText(priceLevels[(int) mPlace.getPriceLevel()]);
            } else {
                priceLevel.setVisibility(View.INVISIBLE);
            }

//            ImageView favouriteView = (ImageView) rootView.findViewById(R.id.favourite);
//            favouriteView.setColorFilter(mPlaceItem.isFavourite() ? Color.RED : Color.GRAY);
//
//            ImageView visitedView = (ImageView) rootView.findViewById(R.id.visited);
//            visitedView.setColorFilter(mPlaceItem.isVisited() ? Color.GREEN : Color.GRAY);
        }

        return rootView;
    }
}
