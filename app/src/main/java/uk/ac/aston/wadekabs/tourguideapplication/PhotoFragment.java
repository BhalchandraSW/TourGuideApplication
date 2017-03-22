package uk.ac.aston.wadekabs.tourguideapplication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceContent;

/**
 * Created by bhalchandrawadekar on 21/03/2017.
 */

public class PhotoFragment extends Fragment {

    public static String PHOTO_INDEX = "photo_index";
    public static String SELECTED_PLACE = "selected_place";

    private NetworkImageView mPhoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        Bundle args = getArguments();
        String placeId = args.getString(SELECTED_PLACE);
        int index = args.getInt(PHOTO_INDEX);

        Place place = PlaceContent.getPlace(placeId);

        View view = inflater.inflate(R.layout.place_photo, container, false);

        mPhoto = (NetworkImageView) view.findViewById(R.id.photo);
        if (place != null && place.getPictures() != null) {

            String photoReference = place.getPictures().get(index);

            String url = "https://maps.googleapis.com/maps/api/place/photo"
                    + "?key=" + "AIzaSyC6EOOcdrhZYb1TgD8xpPlRfPwDHnSddGQ"
                    + "&maxwidth=" + 1000
                    + "&photoreference=" + photoReference;

            mPhoto.setImageUrl(url,
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
        }

        return view;
    }

    public Bitmap getBitmap() {
        return ((BitmapDrawable) mPhoto.getDrawable()).getBitmap();
    }
}
