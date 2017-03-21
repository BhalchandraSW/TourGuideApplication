package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;

/**
 * Created by Bhalchandra Wadekar on 21/03/2017.
 */

public class PlaceSummaryFragment extends Fragment {

    public static final String SELECTED_PLACE_INDEX = "selected_place_index";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        int i = args.getInt(SELECTED_PLACE_INDEX);
        final Place place = PlaceContent.nearby().get(i);

        View view = inflater.inflate(R.layout.place_summary, container, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), NearbyPlaceDetailActivity.class);
                intent.putExtra(PlaceItemDetailFragment.SELECTED_PLACE_ID, place.getPlaceId());
                intent.putExtra(PlaceItemDetailFragment.SELECTED_LIST, "nearby");

                startActivity(intent);
            }
        });

        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        if (place.getPhotos().size() > 0)
            photo.setImageBitmap(place.getPhotos().get(0));

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(place.getName());

        TextView address = (TextView) view.findViewById(R.id.address);
        address.setText(place.getAddress());

        return view;
    }
}
