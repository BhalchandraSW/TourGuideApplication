package uk.ac.aston.wadekabs.tourguideapplication.model;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.util.List;

import uk.ac.aston.wadekabs.tourguideapplication.PlaceItemRecyclerViewAdapter;

/**
 * Created by Bhalchandra Wadekar on 11/03/2017.
 */

class PhotoTask extends AsyncTask<Place, Void, List<Bitmap>> {

    private PlaceItemRecyclerViewAdapter.PlaceItemViewHolder mHolder = null;
    private ImageView mPhoto = null;
    private GoogleApiClient mGoogleApiClient;

    private Place place;

    PhotoTask(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Bitmap> doInBackground(Place... params) {

        if (params.length != 1) {
            return null;
        }

        place = params[0];

        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(mGoogleApiClient, place.getPlaceId()).await();

        if (result.getStatus().isSuccess()) {
            PlacePhotoMetadataBuffer photoMetadata = result.getPhotoMetadata();
            if (photoMetadata.getCount() > 0 && !isCancelled()) {

                for (PlacePhotoMetadata photo : photoMetadata) {
                    // Get the first bitmap and its attributions.
                    CharSequence attribution = photo.getAttributions();

                    // Load a scaled bitmap for this photo.
                    // TODO: convert dp's to pixels here
                    // TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                    photo.getPhoto(mGoogleApiClient).setResultCallback(new ResultCallback<PlacePhotoResult>() {
                        @Override
                        public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                            place.addPhoto(placePhotoResult.getBitmap());
                        }
                    });
                }
            }
            // Release the PlacePhotoMetadataBuffer.
            photoMetadata.release();
        }
        return place.getPhotos();
    }

    @Override
    protected void onPostExecute(List<Bitmap> bitmap) {
        super.onPostExecute(bitmap);

        if (mHolder != null) {

            if (mHolder.imageView != null)
                mHolder.imageView.setImageBitmap(bitmap.get(0));
            mHolder.mItem.addPhoto(bitmap.get(0));
        }

        if (mPhoto != null) {
            mPhoto.setImageBitmap(bitmap.get(0));
        }
    }
}
