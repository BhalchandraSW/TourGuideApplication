package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import uk.ac.aston.wadekabs.tourguideapplication.model.Place;

/**
 * Created by Bhalchandra Wadekar on 11/03/2017.
 */

public class PlaceItemRecyclerViewAdapter extends RecyclerView.Adapter<PlaceItemRecyclerViewAdapter.PlaceItemViewHolder> {

    private String mType;
    private List<Place> mPlaceItemList;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    PlaceItemRecyclerViewAdapter(Context context, List<Place> placeList, GoogleApiClient googleApiClient, String type) {
        mContext = context;
        mPlaceItemList = placeList;
        mGoogleApiClient = googleApiClient;
        mType = type;
    }

    @Override
    public PlaceItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_list_item, parent, false);
        return new PlaceItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceItemViewHolder holder, int position) {

        holder.mItem = mPlaceItemList.get(position);
        holder.name.setText(holder.mItem.getName());
        holder.address.setText(holder.mItem.getAddress());

        String photoReference = holder.mItem.getPictures().get(position);

        String url = "https://maps.googleapis.com/maps/api/place/photo"
                + "?key=" + "AIzaSyC6EOOcdrhZYb1TgD8xpPlRfPwDHnSddGQ"
                + "&maxwidth=" + 1000
                + "&photoreference=" + photoReference;

        holder.photo.setImageUrl(url,
                new ImageLoader(Volley.newRequestQueue(mContext), new ImageLoader.ImageCache() {

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


//        new PhotoTask(holder, mGoogleApiClient).execute(holder.mItem.getPlaceId());

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = v.getContext();
                Intent intent = new Intent(context, FavouritePlaceDetailActivity.class);
                intent.putExtra(PlaceItemDetailFragment.SELECTED_PLACE_ID, holder.mItem.getPlaceId());
                intent.putExtra(PlaceItemDetailFragment.SELECTED_LIST, mType);

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaceItemList.size();
    }

    public class PlaceItemViewHolder extends RecyclerView.ViewHolder {

        public Place mItem;

        final View mItemView;

        public final NetworkImageView photo;
        final TextView name;
        final TextView address;

        PlaceItemViewHolder(View itemView) {

            super(itemView);

            mItemView = itemView;

            photo = (NetworkImageView) itemView.findViewById(R.id.photo);
            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
        }
    }
}
