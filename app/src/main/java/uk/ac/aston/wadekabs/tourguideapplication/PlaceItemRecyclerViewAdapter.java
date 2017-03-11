package uk.ac.aston.wadekabs.tourguideapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItem;
import uk.ac.aston.wadekabs.tourguideapplication.model.PlaceItemContent;

/**
 * Created by Bhalchandra Wadekar on 11/03/2017.
 */

class PlaceItemRecyclerViewAdapter extends RecyclerView.Adapter<PlaceItemRecyclerViewAdapter.PlaceItemViewHolder> {

    private List<PlaceItem> mPlaceItemList;
    private GoogleApiClient mGoogleApiClient;

    PlaceItemRecyclerViewAdapter(List<PlaceItem> placeItemList, GoogleApiClient googleApiClient) {
        mPlaceItemList = placeItemList;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public PlaceItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.placeitem_card, parent, false);
        return new PlaceItemViewHolder(view);
    }

    @Override
    public void onViewDetachedFromWindow(PlaceItemViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onBindViewHolder(final PlaceItemViewHolder holder, int position) {
        holder.mItem = PlaceItemContent.getInstance().getPlaceItemList().get(position);
        holder.nameTextView.setText(holder.mItem.getTitle());
        holder.addressTextView.setText(holder.mItem.getAddress());

        new PhotoTask(holder, mGoogleApiClient).execute(holder.mItem.getId());

        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, PlaceItemDetailActivity.class);
                intent.putExtra(PlaceItemDetailFragment.SELECTED_PLACE_ITEM, holder.getAdapterPosition());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaceItemList.size();
    }

    class PlaceItemViewHolder extends RecyclerView.ViewHolder {

        PlaceItem mItem;

        final CardView mItemView;
        final ImageView imageView;

        final TextView nameTextView;
        final TextView addressTextView;
        final TextView descriptionTextView;

        PlaceItemViewHolder(CardView itemView) {

            super(itemView);

            mItemView = itemView;

            imageView = (ImageView) itemView.findViewById(R.id.imageView);

            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            addressTextView = (TextView) itemView.findViewById(R.id.addressTextView);
            descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        }
    }
}
