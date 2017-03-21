package uk.ac.aston.wadekabs.tourguideapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;

/**
 * Created by bhalchandrawadekar on 21/03/2017.
 */

public class PhotoFragment extends Fragment {

    public static String PHOTO_INDEX = "photo_index";
    public static String SELECTED_PLACE = "selected_place";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        String placeId = args.getString(SELECTED_PLACE);
        int index = args.getInt(PHOTO_INDEX);

        Place place = PlaceContent.getPlace(placeId);

        View view = inflater.inflate(R.layout.place_photo, container, false);

        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        if (place != null) {
            photo.setImageBitmap(place.getPhotos().get(index));
        }

        return view;
    }
}
