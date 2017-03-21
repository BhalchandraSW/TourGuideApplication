package uk.ac.aston.wadekabs.tourguideapplication.model;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import uk.ac.aston.wadekabs.tourguideapplication.PlaceItemRecyclerViewAdapter;

/**
 * Created by Bhalchandra Wadekar on 11/03/2017.
 */

class PhotoTask extends AsyncTask<Place, Void, Bitmap> {

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
    protected Bitmap doInBackground(Place... params) {

        if (params.length != 1) {
            return null;
        }

        place = params[0];
        final String placeId = params[0].getPlaceId();
        Bitmap image = null;

        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(mGoogleApiClient, placeId).await();

        if (result.getStatus().isSuccess()) {
            PlacePhotoMetadataBuffer photoMetadata = result.getPhotoMetadata();
            if (photoMetadata.getCount() > 0 && !isCancelled()) {

                // Get the first bitmap and its attributions.
                PlacePhotoMetadata photo = photoMetadata.get(0);
                CharSequence attribution = photo.getAttributions();

                // Load a scaled bitmap for this photo.
                // TODO: convert dp's to pixels here
                // TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                image = photo.getPhoto(mGoogleApiClient).await()
                        .getBitmap();
            }
            // Release the PlacePhotoMetadataBuffer.
            photoMetadata.release();
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        place.addPhoto(bitmap);

        if (mHolder != null) {

            if (mHolder.imageView != null)
                mHolder.imageView.setImageBitmap(bitmap);
            mHolder.mItem.addPhoto(bitmap);
        }

        if (mPhoto != null) {
            mPhoto.setImageBitmap(bitmap);
        }
    }
}
