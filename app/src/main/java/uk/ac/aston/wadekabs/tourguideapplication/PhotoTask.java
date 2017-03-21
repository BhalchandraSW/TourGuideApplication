package uk.ac.aston.wadekabs.tourguideapplication;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

/**
 * Created by Bhalchandra Wadekar on 11/03/2017.
 */

class PhotoTask extends AsyncTask<String, Void, Bitmap> {

    private PlaceItemRecyclerViewAdapter.PlaceItemViewHolder mHolder = null;
    private ImageView mPhoto = null;
    private GoogleApiClient mGoogleApiClient;

    PhotoTask(PlaceItemRecyclerViewAdapter.PlaceItemViewHolder holder, GoogleApiClient googleApiClient) {
        mHolder = holder;
        mGoogleApiClient = googleApiClient;
    }

    PhotoTask(ImageView photo, GoogleApiClient googleApiClient) {
        mPhoto = photo;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        if (params.length != 1) {
            return null;
        }

        final String placeId = params[0];
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

        if (mHolder != null) {

            if (mHolder.imageView != null)
                mHolder.imageView.setImageBitmap(bitmap);
            mHolder.mItem.setPhoto(bitmap);
        }

        if (mPhoto != null) {
            mPhoto.setImageBitmap(bitmap);
        }
    }
}
