package uk.ac.aston.wadekabs.tourguideapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItem;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItemContent;

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
    public static final String SELECTED_PLACE_ITEM = "selected_place_item";

    /**
     * The dummy content this fragment is presenting.
     */
    private int mPlaceItemPosition;
    private PlaceItem mPlaceItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlaceItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(SELECTED_PLACE_ITEM)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mPlaceItemPosition = getArguments().getInt(SELECTED_PLACE_ITEM);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                mPlaceItem = PlaceItemContent.getInstance().getPlaceItemList().get(mPlaceItemPosition);
                appBarLayout.setTitle(mPlaceItem.getTitle());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.placeitem_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mPlaceItem != null) {

            ((TextView) rootView.findViewById(R.id.placeitem_detail)).setText(mPlaceItem.getAddress());

            ImageView favouriteView = (ImageView) rootView.findViewById(R.id.favourite);
            favouriteView.setColorFilter(mPlaceItem.isFavourite() ? Color.RED : Color.GRAY);

            ImageView visitedView = (ImageView) rootView.findViewById(R.id.visited);
            visitedView.setColorFilter(mPlaceItem.isVisited() ? Color.GREEN : Color.GRAY);
        }

        return rootView;
    }
}
