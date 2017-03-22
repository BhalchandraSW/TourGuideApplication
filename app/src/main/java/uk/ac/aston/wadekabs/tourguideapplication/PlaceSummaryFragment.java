package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

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

        String photoReference = place.getPictures().get(0);

        String url = "https://maps.googleapis.com/maps/api/place/photo"
                + "?key=" + "AIzaSyC6EOOcdrhZYb1TgD8xpPlRfPwDHnSddGQ"
                + "&maxwidth=" + 1000
                + "&photoreference=" + photoReference;

        NetworkImageView photo = (NetworkImageView) view.findViewById(R.id.photo);
        photo.setImageUrl(url,
                new ImageLoader(Volley.newRequestQueue(getContext()), new ImageLoader.ImageCache() {

                    private final LruCache<String, Bitmap>
                            cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                }));

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(place.getName());

        TextView address = (TextView) view.findViewById(R.id.address);
        address.setText(place.getAddress());

        return view;
    }
}
